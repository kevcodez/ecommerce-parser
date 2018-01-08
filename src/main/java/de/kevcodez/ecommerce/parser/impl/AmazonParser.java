package de.kevcodez.ecommerce.parser.impl;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.kevcodez.ecommerce.parser.domain.image.Image;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;
import lombok.SneakyThrows;

public class AmazonParser extends JsoupProductParser {

    private static final Pattern PATTERN_IMAGES = Pattern
        .compile("(?<='colorImages':.\\{.'initial':.)([\\S\\s]+)(?=},\\s+'colorToAsin')");

    private static final Pattern PATTERN_DISCOUNT = Pattern.compile("(\\d+,\\d+).\\((\\d+)%\\)");

    private static final Pattern PATTERN_PRICE = Pattern.compile("\\d+.\\d+");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AmazonParser(WebsiteSourceDownloader websiteSourceDownloader) {
        super(websiteSourceDownloader);
    }

    @Override
    public List<String> supportedDomains() {
        return asList("amazon.de", "amazon.com");
    }

    @Override
    String parseTitle(Document document) {
        return document.select("span#productTitle").text();
    }

    @Override
    String parseDescription(Document document) {
        return document.select("div#productDescription > p:first-child").text();
    }

    @Override
    BigDecimal parseCurrentPrice(Document document) {
        String priceAsString = document.select("span.a-size-medium.a-color-price.offer-price.a-text-normal").text();

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_ourprice").text();
        }

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_saleprice").text();
        }

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_dealprice").text();
        }

        return priceStringToBigDecimal(priceAsString);
    }

    @Override
    String parseCurrencyCode(String url, Document document) {
        if (url.contains("amazon.de")) {
            return "EUR";
        }
        else if (url.contains("amazon.com")) {
            return "USD";
        }

        throw new IllegalArgumentException("Currency code could not be parsed");
    }

    @Override
    Optional<Discount> parseDiscount(BigDecimal currentPrice, Document document) {
        Discount discount = null;

        String discountAsText = document.select("tr#regularprice_savings > td.a-color-price").text();
        if (discountAsText != null) {
            Matcher matcherDiscount = PATTERN_DISCOUNT.matcher(discountAsText);
            if (matcherDiscount.find()) {
                String oldPriceAsText = document.select("div#price span.a-text-strike").text();
                BigDecimal oldPrice = priceStringToBigDecimal(oldPriceAsText);
                BigDecimal discountValue = priceStringToBigDecimal(matcherDiscount.group(1));
                BigDecimal percentage = new BigDecimal(matcherDiscount.group(2));

                discount = new Discount()
                    .setOldPrice(oldPrice)
                    .setDiscount(discountValue)
                    .setPercentage(percentage);
            }
        }

        return Optional.ofNullable(discount);
    }

    private BigDecimal priceStringToBigDecimal(String text) {
        Matcher matcher = PATTERN_PRICE.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group().replace(",", "."));
        }
        else {
            throw new IllegalArgumentException("Could not find price");
        }
    }

    @Override
    String parseExternalId(Document document) {
        return document.select("input#ASIN").val();
    }

    @SneakyThrows
    @Override
    List<Image> parseImages(Document document) {
        TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<Map<String, List<Integer>>>() {

        };

        List<Image> images = new ArrayList<>();

        Matcher matcher = PATTERN_IMAGES.matcher(document.html());
        if (matcher.find()) {
            String colorImages = matcher.group();

            ArrayNode imagesAsJson = (ArrayNode) OBJECT_MAPPER.readTree(colorImages);
            imagesAsJson.forEach(node -> {
                Image image = new Image();

                Map<String, List<Integer>> imageMap = OBJECT_MAPPER.convertValue(node.get("main"), typeRef);
                imageMap.forEach((key, value) -> image.addVariant(ImageVariant.builder()
                    .url(key)
                    .height(value.get(0))
                    .width(value.get(1))
                    .build()));

                images.add(image);
            });
        }

        return images;
    }

}
