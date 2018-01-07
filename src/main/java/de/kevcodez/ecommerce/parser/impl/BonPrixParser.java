package de.kevcodez.ecommerce.parser.impl;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Document;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;

public class BonPrixParser extends AbstractProductParser {

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
        // TODO
        return Collections.emptyList();
    }
}
