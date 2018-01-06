package de.kevcodez.ecommerce.parser.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import lombok.SneakyThrows;

public class AmazonParser extends AbstractProductParser {

    private static final Pattern PATTERN_AMAZON = Pattern.compile("((http(s?)://)?(www\\.)?)amazon\\.(.+)");

    private static final Pattern PATTERN_IMAGES = Pattern
        .compile("(?<='colorImages':.\\{.'initial':.)([\\S\\s]+)(?=},\\s+'colorToAsin')");

    private static final Pattern PATTERN_DISCOUNT = Pattern.compile("(\\d+,\\d+).\\((\\d+)%\\)");

    private static final Pattern PATTERN_PRICE = Pattern.compile("\\d+,\\d+");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AmazonParser(WebsiteSourceDownloader websiteSourceDownloader) {
        super(websiteSourceDownloader);
    }

    @Override
    public boolean matches(String url) {
        return PATTERN_AMAZON.matcher(url).matches();
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
    Price parsePrice(Document document) {
        String price = document.select("span.a-size-medium.a-color-price.offer-price.a-text-normal").text();

        if (price.isEmpty()) {
            price = document.select("span#priceblock_ourprice").text();
        }

        if (price.isEmpty()) {
            price = document.select("span#priceblock_saleprice").text();
        }

        Discount discount = parseDiscount(document);

        return new Price(priceStringToBigDecimal(price), "EUR", discount);
    }

    private Discount parseDiscount(Document document) {
        String discountAsText = document.select("tr#regularprice_savings > td.a-color-price").text();
        if (discountAsText != null) {
            Matcher matcherDiscount = PATTERN_DISCOUNT.matcher(discountAsText);
            if (matcherDiscount.find()) {
                String oldPriceAsText = document.select("div#price span.a-text-strike").text();
                BigDecimal oldPrice = priceStringToBigDecimal(oldPriceAsText);
                BigDecimal discountValue = priceStringToBigDecimal(matcherDiscount.group(1));
                BigDecimal percentage = new BigDecimal(matcherDiscount.group(2));
                return new Discount(oldPrice, discountValue, percentage);
            }
        }

        return null;
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
    List<ImageDto> parseImages(Document document) {
        TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<Map<String, List<Integer>>>() {

        };

        List<ImageDto> images = new ArrayList<>();

        Matcher matcher = PATTERN_IMAGES.matcher(document.html());
        if (matcher.find()) {
            String colorImages = matcher.group();

            ArrayNode imagesAsJson = (ArrayNode) OBJECT_MAPPER.readTree(colorImages);
            imagesAsJson.forEach(node -> {
                ImageDto imageDto = new ImageDto();

                Map<String, List<Integer>> imageMap = OBJECT_MAPPER.convertValue(node.get("main"), typeRef);
                imageMap.forEach((key, value) -> {
                    imageDto.addVariant(ImageVariant.builder()
                        .url(key)
                        .height(value.get(0))
                        .width(value.get(1))
                        .build());
                });

                images.add(imageDto);
            });
        }

        return images;
    }

}
