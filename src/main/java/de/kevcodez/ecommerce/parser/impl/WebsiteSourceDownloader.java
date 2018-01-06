package de.kevcodez.ecommerce.parser.impl;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;

import lombok.SneakyThrows;

public class WebsiteSourceDownloader {

    @SneakyThrows
    public String download(String url) {
        return new String(Jsoup.connect(url).execute().bodyAsBytes(), StandardCharsets.UTF_8);
    }

}
