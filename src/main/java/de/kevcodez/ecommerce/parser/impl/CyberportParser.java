package de.kevcodez.ecommerce.parser.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;

public class CyberportParser extends JsoupProductParser {

    private static final Pattern PATTERN_EXTERNAL_ID = Pattern.compile("([A-Z]|[0-9])+-([A-Z]|[0-9])+_([A-Z]|[0-9])+");

    public CyberportParser(WebsiteSourceDownloader websiteSourceDownloader) {
        super(websiteSourceDownloader);
    }

    @Override
    List<String> supportedDomains() {
        return Collections.singletonList("cyberport.de");
    }

    @Override
    String parseExternalId(Document document) {
        String url = document.select("form#loginformleft").attr("action");

        Matcher matcher = PATTERN_EXTERNAL_ID.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalArgumentException("Parsing external id failed");
    }

    @Override
    String parseTitle(Document document) {
        return document.select("h1 > span[itemprop='name']").text().trim();
    }

    @Override
    String parseDescription(Document document) {
        return document.select("div.article > p").html();
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
        String oldPriceAsString = document.select("div.old-price2 > div").text();

        if (!oldPriceAsString.isEmpty()) {
            String formattedPrice = oldPriceAsString.replace(".", ",").replace(",", ".");
            BigDecimal oldPrice = new BigDecimal(formattedPrice);

            return Discount.of(oldPrice, currentPrice);
        }

        return null;
    }

    @Override
    List<ImageDto> parseImages(Document document) {
        return Collections.emptyList();
    }
}
