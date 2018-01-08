package de.kevcodez.ecommerce.parser.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.kevcodez.ecommerce.parser.domain.image.Image;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader;

public abstract class JsoupProductParser implements ProductParser {

    private final WebsiteSourceDownloader websiteSourceDownloader;

    JsoupProductParser(WebsiteSourceDownloader websiteSourceDownloader) {
        this.websiteSourceDownloader = websiteSourceDownloader;
    }

    @Override
    public Product parse(String url) {
        String websiteSource = websiteSourceDownloader.download(url);

        Document document = Jsoup.parse(websiteSource, StandardCharsets.UTF_8.name());

        String externalId = parseExternalId(document);
        String title = parseTitle(document);
        String description = parseDescription(document);

        BigDecimal currentPrice = parseCurrentPrice(document);
        String currencyCode = parseCurrencyCode(url, document);
        Discount discount = parseDiscount(currentPrice, document);

        Price price = new Price()
            .setCurrentPrice(currentPrice)
            .setCurrency(currencyCode)
            .setDiscount(discount);

        List<Image> images = parseImages(document);

        return Product.builder()
            .url(url)
            .title(title)
            .description(description)
            .price(price)
            .externalId(externalId)
            .images(images)
            .build();
    }

    public boolean matches(String domain) {
        return supportedDomains().contains(domain);
    }

    abstract List<String> supportedDomains();

    abstract String parseExternalId(Document document);

    abstract String parseTitle(Document document);

    abstract String parseDescription(Document document);

    abstract BigDecimal parseCurrentPrice(Document document);

    abstract String parseCurrencyCode(String url, Document document);

    abstract Discount parseDiscount(BigDecimal currentPrice, Document document);

    abstract List<Image> parseImages(Document document);

}
