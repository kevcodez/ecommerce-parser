package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.util.*
import java.util.Collections.singletonList
import java.util.regex.Pattern

class AlternateParser(websiteSourceDownloader: WebsiteSourceDownloader) : JsoupProductParser(websiteSourceDownloader) {

    override fun supportedDomains(): List<String> {
        return singletonList("alternate.de")
    }

    override fun parseExternalId(document: Document): String {
        return document.select("var#expressTickerProductId").text()
    }

    override fun parseTitle(document: Document): String {
        val nameElements = document.select("div.productNameContainer > h1 > span")

        return nameElements[0].text() + " " + nameElements[1].text()
    }

    override fun parseDescription(document: Document): String {
        return document.select("div.description > p:first-child").text()
    }

    override fun parseCurrentPrice(document: Document): BigDecimal {
        val price = document.select("div.price").attr("data-standard-price")

        return BigDecimal(price)
    }

    override fun parseCurrencyCode(url: String, document: Document): String {
        return "EUR"
    }

    override fun parseDiscount(currentPrice: BigDecimal, document: Document): Optional<Discount> {
        var discount: Discount? = null

        val discountAsText = document.select("div.productShort > span.strikedPrice").text()

        if (discountAsText != null) {
            val matcher = PATTERN_DISCOUNT.matcher(discountAsText)
            if (matcher.find()) {
                val previousPrice = BigDecimal(matcher.group().replace(",", "."))
                discount = Discount.of(previousPrice, currentPrice)
            }
        }

        return Optional.ofNullable(discount)
    }

    override fun parseImages(document: Document): List<Image> {
        val articleId = document.select("input[name='articleId']").attr("content").toLowerCase()

        var count = document.select("ul.jsSlickCarousel > li").size
        // If no carousel is present, there is only a single image
        if (count == 0) {
            count = 1
        }

        val images = ArrayList<Image>()

        for (i in 0 until count) {
            val suffix = if (i == 0) "" else "_" + i

            val image = Image()
            image.addVariant(ImageVariant(
                    url = "$ALTERNATE_URL/p/230x230/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@$articleId$suffix.jpg",
                    width = 230,
                    height = 230))

            image.addVariant(ImageVariant(
                    url = "$ALTERNATE_URL/p/50x50/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@$articleId$suffix.jpg",
                    width = 50,
                    height = 50))

            images.add(image)
        }

        return images
    }

    companion object {

        private val PATTERN_DISCOUNT = Pattern.compile("""\d+,\d+""")

        private val ALTERNATE_URL = "https://www.alternate.de"
    }
}
