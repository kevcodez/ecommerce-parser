package de.kevcodez.ecommerce.parser.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import de.kevcodez.ecommerce.parser.exception.ParserException
import org.jsoup.nodes.Document
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.Arrays.asList
import java.util.regex.Pattern

class AmazonParser(websiteSourceDownloader: WebsiteSourceDownloader) : JsoupProductParser(websiteSourceDownloader) {

    public override fun supportedDomains(): List<String> {
        return asList("amazon.de", "amazon.com")
    }

    override fun parseTitle(document: Document): String {
        return document.select("span#productTitle").text()
    }

    override fun parseDescription(document: Document): String {
        return document.select("div#productDescription > p:first-child").text()
    }

    override fun parseCurrentPrice(document: Document): BigDecimal {
        var priceAsString = document.select("span.a-size-medium.a-color-price.offer-price.a-text-normal").text()

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_ourprice").text()
        }

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_saleprice").text()
        }

        if (priceAsString.isEmpty()) {
            priceAsString = document.select("span#priceblock_dealprice").text()
        }

        return priceStringToBigDecimal(priceAsString)
    }

    override fun parseCurrencyCode(url: String, document: Document): String {
        if (url.contains("amazon.de")) {
            return "EUR"
        } else if (url.contains("amazon.com")) {
            return "USD"
        }

        throw ParserException("Currency code could not be parsed")
    }

    override fun parseDiscount(currentPrice: BigDecimal, document: Document): Optional<Discount> {
        var discount: Discount? = null

        val discountAsText = document.select("tr#regularprice_savings > td.a-color-price").text()
        if (discountAsText != null) {
            val matcherDiscount = PATTERN_DISCOUNT.matcher(discountAsText)
            if (matcherDiscount.find()) {
                val oldPriceAsText = document.select("div#price span.a-text-strike").text()
                val oldPrice = priceStringToBigDecimal(oldPriceAsText)
                val discountValue = priceStringToBigDecimal(matcherDiscount.group(1))
                val percentage = BigDecimal(matcherDiscount.group(2))

                discount = Discount(oldPrice, discountValue, percentage)
            }
        }

        return Optional.ofNullable(discount)
    }

    private fun priceStringToBigDecimal(text: String): BigDecimal {
        val matcher = PATTERN_PRICE.matcher(text)
        return if (matcher.find()) {
            BigDecimal(matcher.group().replace(",", "."))
        } else {
            throw ParserException("Could not find price")
        }
    }

    override fun parseExternalId(document: Document): String {
        return document.select("input#ASIN").`val`()
    }

    override fun parseImages(document: Document): List<Image> {
        val typeRef = object : TypeReference<Map<String, List<Int>>>() {

        }

        val images = ArrayList<Image>()

        val matcher = PATTERN_IMAGES.matcher(document.html())
        if (matcher.find()) {
            val colorImages = matcher.group()

            val imagesAsJson = convertImageJson(colorImages)
            imagesAsJson.forEach {
                val image = Image()

                val imageMap: Map<String, List<Int>> = OBJECT_MAPPER.convertValue(it.get("main"), typeRef)
                imageMap.forEach({ key, value ->
                    image.addVariant(ImageVariant(
                            url = key,
                            height = value[0],
                            width = value[1]))
                })

                images.add(image)
            }
        }

        return images
    }

    private fun convertImageJson(json: String): ArrayNode {
        return try {
            OBJECT_MAPPER.readTree(json) as ArrayNode
        } catch (exc: IOException) {
            throw ParserException("Exception parsing images", exc)
        }
    }

    companion object {

        private val PATTERN_IMAGES = Pattern
                .compile("""(?<='colorImages':.\{.'initial':.)([\S\s]+)(?=},\s+'colorToAsin')""")

        private val PATTERN_DISCOUNT = Pattern.compile("""(\d+,\d+).\((\d+)%\)""")

        private val PATTERN_PRICE = Pattern.compile("""\d+.\d+""")

        private val OBJECT_MAPPER = ObjectMapper()
    }

}
