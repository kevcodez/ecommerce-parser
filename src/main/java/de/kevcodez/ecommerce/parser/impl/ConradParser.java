package de.kevcodez.ecommerce.parser.impl;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import de.kevcodez.ecommerce.parser.domain.image.Image;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;

public class ConradParser extends JsoupProductParser {

    private static Pattern PATTERN_IMG_DIMENSIONS = Pattern.compile("\\?x=(\\d+)&y=(\\d+)");

    public ConradParser(WebsiteSourceDownloader websiteSourceDownloader) {
        super(websiteSourceDownloader);
    }

    @Override
    List<String> supportedDomains() {
        return asList("conrad.de", "conrad.it");
    }

    @Override
    String parseExternalId(Document document) {
        return document.select("span[itemprop='sku']").text();
    }

    @Override
    String parseTitle(Document document) {
        return document.select("h1.ccpProductDetail__title__text").text();
    }

    @Override
    String parseDescription(Document document) {
        Element descriptionSection = document.select("div#description > section").get(0);
        return descriptionSection.textNodes().stream()
            .filter(node -> !node.isBlank())
            .map(TextNode::getWholeText)
            .collect(Collectors.joining("\n"))
            .trim();
    }

    @Override
    BigDecimal parseCurrentPrice(Document document) {
        String priceAsString = document.select("meta[itemprop='price']").attr("content");

        return new BigDecimal(priceAsString);
    }

    @Override
    String parseCurrencyCode(String url, Document document) {
        return "EUR";
    }

    @Override
    Discount parseDiscount(BigDecimal currentPrice, Document document) {
        String discountAsText = document.select("div.ccpProductDetailInfo__cell__price__old__value > span").text();

        if (!discountAsText.isEmpty()) {
            String oldPriceAsString = discountAsText.replace(" €", "").replace(",", ".");
            return Discount.of(new BigDecimal(oldPriceAsString), currentPrice);
        }

        return null;
    }

    @Override
    List<Image> parseImages(Document document) {
        Elements imageElements = document.select("img.ccpProductDetailSlideshow__slider__wrapper__list__item__image");

        List<Image> images = new ArrayList<>();

        imageElements.forEach(element -> {
            String url = element.attr("src");

            Image image = new Image();
            Set<String> variants = findAllImageUrls(document, url);

            variants.stream()
                .map(this::buildByUrl)
                .forEach(image::addVariant);

            images.add(image);
        });

        return images;
    }

    private Set<String> findAllImageUrls(Document document, String imageUrl) {
        String urlWithoutParameters = imageUrl.split("\\?")[0];

        Set<String> urls = new HashSet<>();

        Elements imgElements = document.select("img");
        imgElements.stream().map(element -> element.attr("src"))
            .filter(src -> src.startsWith(urlWithoutParameters))
            .forEach(urls::add);

        return urls;
    }

    private ImageVariant buildByUrl(String url) {
        Matcher matcher = PATTERN_IMG_DIMENSIONS.matcher(url);

        if (matcher.find()) {
            return ImageVariant.builder()
                .url(url)
                .height(Integer.parseInt(matcher.group(2)))
                .width(Integer.parseInt(matcher.group(1)))
                .build();
        }

        throw new IllegalArgumentException("Error parsing image variant " + url);
    }
}
