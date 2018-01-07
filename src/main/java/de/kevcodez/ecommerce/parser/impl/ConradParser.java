package de.kevcodez.ecommerce.parser.impl;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.price.Discount;

public class ConradParser extends AbstractProductParser {

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
    List<ImageDto> parseImages(Document document) {
        // TODO
        return Collections.emptyList();
    }
}
