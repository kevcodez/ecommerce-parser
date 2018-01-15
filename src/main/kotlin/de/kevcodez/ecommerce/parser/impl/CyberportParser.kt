package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import de.kevcodez.ecommerce.parser.exception.ParserException
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern

class CyberportParser(websiteSourceDownloader: WebsiteSourceDownloader) : JsoupProductParser(websiteSourceDownloader) {

    override fun supportedDomains(): List<String> {
        return Collections.singletonList("cyberport.de")
    }

    override fun parseExternalId(document: Document): String {
        val url = document.select("form#loginformleft").attr("action")

        val matcher = PATTERN_EXTERNAL_ID.matcher(url)

        if (matcher.find()) {
            return matcher.group()
        }

        throw ParserException("Parsing external id failed")
    }

    override fun parseTitle(document: Document): String {
        return document.select("h1 > span[itemprop='name']").text().trim()
    }

    override fun parseDescription(document: Document): String {
        return document.select("div.article > p").html()
    }

    override fun parseCurrentPrice(document: Document): BigDecimal {
        val priceAsString = document.select("meta[itemprop='price']").attr("content")
        return BigDecimal(priceAsString)
    }

    override fun parseCurrencyCode(url: String, document: Document): String {
        return "EUR"
    }

    override fun parseDiscount(currentPrice: BigDecimal, document: Document): Optional<Discount> {
        var discount: Discount? = null

        val oldPriceAsString = document.select("div.old-price2 > div").text()

        if (!oldPriceAsString.isEmpty()) {
            val formattedPrice = oldPriceAsString.replace(".", ",").replace(",", ".")
            val oldPrice = BigDecimal(formattedPrice)

            discount = Discount.of(oldPrice, currentPrice)
        }

        return Optional.ofNullable(discount)
    }

    override fun parseImages(document: Document): List<Image> {
        return Collections.emptyList()
    }

    companion object {

        private val PATTERN_EXTERNAL_ID = Pattern.compile("""([A-Z]|[0-9])+-([A-Z]|[0-9])+_([A-Z]|[0-9])+""")
    }
}
