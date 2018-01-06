package de.kevcodez.ecommerce.parser.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

public abstract class AbstractProductParser {

    private WebsiteSourceDownloader websiteSourceDownloader;

    public AbstractProductParser(WebsiteSourceDownloader websiteSourceDownloader) {
        this.websiteSourceDownloader = websiteSourceDownloader;
    }

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

    public abstract boolean matches(String url);

    abstract String parseExternalId(Document document);

    abstract String parseTitle(Document document);

    abstract String parseDescription(Document document);

    abstract Price parsePrice(Document document);

    abstract List<ImageDto> parseImages(Document document);

}
