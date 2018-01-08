package de.kevcodez.ecommerce.parser.impl;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;

public class BonPrixParser extends JsoupProductParser {

    private static final Pattern PATTERN_IMG_DIMENSIONS = Pattern.compile("\\/(\\d+)x(\\d+)\\/");

    public BonPrixParser(WebsiteSourceDownloader websiteSourceDownloader) {
        super(websiteSourceDownloader);
    }

    @Override
    List<String> supportedDomains() {
        return singletonList("bonprix.de");
    }

    @Override
    String parseExternalId(Document document) {
        return document.select("div#product-page").attr("data-product-ordernumber");
    }

    @Override
    String parseTitle(Document document) {
        return document.select("h1.product-name").text();
    }

    @Override
    String parseDescription(Document document) {
        return document.select("p.product-information-full-description").text();
    }

    @Override
    BigDecimal parseCurrentPrice(Document document) {
        String priceAsString = document.select("div#offer > span.price").attr("content");
        return new BigDecimal(priceAsString);
    }

    @Override
    String parseCurrencyCode(String url, Document document) {
        return "EUR";
    }

    @Override
    Discount parseDiscount(BigDecimal currentPrice, Document document) {
        String formerPrice = document.select("span.price.former-price").text();

        if (formerPrice.isEmpty()) {
            return null;
        }

        String oldPriceAsString = formerPrice.replace("€", "").replace(",", ".");

        return Discount.of(new BigDecimal(oldPriceAsString), currentPrice);
    }

    @Override
    List<ImageDto> parseImages(Document document) {
        Elements imageWrapperElements = document.select("div#carousel_product_look div.image-wrapper");

        List<ImageDto> images = new ArrayList<>();

        imageWrapperElements.forEach(element -> {
            ImageDto image = new ImageDto();

            String dataImageSrc = element.attr("data-image-src");
            String dataZoomImageSrc = element.attr("data-zoom-image-src");
            String dataPreviewImageSrc = element.attr("data-preview-image-src");

            image.addVariant(buildImageVariantFromUrl(dataImageSrc));
            image.addVariant(buildImageVariantFromUrl(dataZoomImageSrc));
            image.addVariant(buildImageVariantFromUrl(dataPreviewImageSrc));

            images.add(image);
        });

        return images;
    }

    private ImageVariant buildImageVariantFromUrl(String imageSource) {
        Matcher matcher = PATTERN_IMG_DIMENSIONS.matcher(imageSource);
        if (matcher.find()) {
            int height = Integer.parseInt(matcher.group(1));
            int width = Integer.parseInt(matcher.group(2));

            return ImageVariant.builder()
                .url("https:" + imageSource)
                .height(height)
                .width(width)
                .build();
        }

        throw new IllegalArgumentException("Error parsing images with url " + imageSource);
    }

}
