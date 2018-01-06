package de.kevcodez.ecommerce.parser.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

public class AlternateParser implements ProductParser {

    private static final Pattern PATTERN_URL = Pattern.compile("((http(s?)://)?(www\\.)?)alternate\\.(.+)");

    private static final Pattern PATTERN_DISCOUNT = Pattern.compile("\\d+,\\d+");

    private static final String ALTERNATE_URL = "https://www.alternate.de";

    private WebsiteSourceDownloader websiteSourceDownloader;

    public AlternateParser(WebsiteSourceDownloader websiteSourceDownloader) {
        this.websiteSourceDownloader = websiteSourceDownloader;
    }

    @Override
    public boolean matches(String url) {
        return PATTERN_URL.matcher(url).matches();
    }

    @Override
    public Product parse(String url) {
        String websiteSource = websiteSourceDownloader.download(url);

        Document document = Jsoup.parse(websiteSource, StandardCharsets.UTF_8.name());

        String externalId = parseExternalId(document);
        String title = parseTitle(document);
        String description = parseDescription(document);
        Price price = parsePrice(document);
        List<ImageDto> images = parseImages(document);

        return Product.builder()
            .url(url)
            .title(title)
            .description(description)
            .price(price)
            .externalId(externalId)
            .images(images)
            .build();
    }

    private String parseExternalId(Document document) {
        return document.select("var#expressTickerProductId").text();
    }

    private String parseTitle(Document document) {
        Elements nameElements = document.select("div.productNameContainer > h1 > span");

        return nameElements.get(0).text() + " " + nameElements.get(1).text();
    }

    private String parseDescription(Document document) {
        return document.select("div.description > p:first-child").text();
    }

    private Price parsePrice(Document document) {
        String price = document.select("div.price").attr("data-standard-price");

        BigDecimal currentPrice = new BigDecimal(price);
        Discount discount = parseDiscount(document, currentPrice);

        return new Price(currentPrice, "EUR", discount);
    }

    private Discount parseDiscount(Document document, BigDecimal currentPrice) {
        String discountAsText = document.select("div.productShort > span.strikedPrice").text();

        if (discountAsText != null) {
            Matcher matcher = PATTERN_DISCOUNT.matcher(discountAsText);
            if (matcher.find()) {
                BigDecimal previousPrice = new BigDecimal(matcher.group().replace(",", "."));
                return Discount.of(previousPrice, currentPrice);
            }
        }

        return null;
    }

    private List<ImageDto> parseImages(Document document) {
        String articleId = document.select("input[name='articleId']").attr("content").toLowerCase();

        int count = document.select("ul.jsSlickCarousel > li").size();
        if (count == 0) {
            count = 1;
        }

        List<ImageDto> images = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String suffix = i == 0 ? "" : String.valueOf(i);

            ImageDto imageDto = new ImageDto();
            imageDto.addVariant(ImageVariant.builder()
                .url(ALTERNATE_URL + "/p/230x230/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@" + articleId + suffix + ".jpg")
                .width(230)
                .height(230)
                .build());

            imageDto.addVariant(ImageVariant.builder()
                .url(ALTERNATE_URL + "/p/50x50/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@" + articleId + suffix + ".jpg")
                .width(50)
                .height(50)
                .build());

            imageDto.addVariant(ImageVariant.builder()
                .url(ALTERNATE_URL + "/p/o/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@" + articleId + suffix + ".jpg")
                .width(50)
                .height(50)
                .build());

            images.add(imageDto);
        }

        return images;
    }
}
