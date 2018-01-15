package de.kevcodez.ecommerce.parser.downloader

import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets

class JsoupSourceDownloader : WebsiteSourceDownloader {

    override fun download(url: String): String {
        return String(Jsoup.connect(url).execute().bodyAsBytes(), StandardCharsets.UTF_8)
    }

}
