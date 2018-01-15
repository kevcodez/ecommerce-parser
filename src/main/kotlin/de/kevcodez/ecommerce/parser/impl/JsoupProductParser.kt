package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.domain.price.Price
import de.kevcodez.ecommerce.parser.domain.product.Product
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.*

abstract class JsoupProductParser(private val websiteSourceDownloader: WebsiteSourceDownloader) : ProductParser {

    override fun parse(url: String): Product {
        val websiteSource = websiteSourceDownloader.download(url)

        val document = Jsoup.parse(websiteSource, StandardCharsets.UTF_8.name())

        val externalId = parseExternalId(document)
        val title = parseTitle(document)
        val description = parseDescription(document)

        val currentPrice = parseCurrentPrice(document)
        val currencyCode = parseCurrencyCode(url, document)
        val discount = parseDiscount(currentPrice, document)

        val price = Price(currentPrice = currentPrice, currency = Currency.getInstance(currencyCode), discount = discount.orElse(null))

        val images = parseImages(document)

        return Product(
                url = url,
                title = title,
                description = description,
                price = price,
                externalId = externalId,
                images = images.toMutableList())
    }

    override fun matches(domain: String): Boolean {
        return supportedDomains().contains(domain)
    }

    internal abstract fun supportedDomains(): List<String>

    internal abstract fun parseExternalId(document: Document): String

    internal abstract fun parseTitle(document: Document): String

    internal abstract fun parseDescription(document: Document): String

    internal abstract fun parseCurrentPrice(document: Document): BigDecimal

    internal abstract fun parseCurrencyCode(url: String, document: Document): String

    internal abstract fun parseDiscount(currentPrice: BigDecimal, document: Document): Optional<Discount>

    internal abstract fun parseImages(document: Document): List<Image>

}
