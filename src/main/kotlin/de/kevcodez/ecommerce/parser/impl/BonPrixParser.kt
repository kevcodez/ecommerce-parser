package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import de.kevcodez.ecommerce.parser.exception.ParserException
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.util.*
import java.util.Collections.singletonList
import java.util.regex.Pattern

class BonPrixParser(websiteSourceDownloader: WebsiteSourceDownloader) : JsoupProductParser(websiteSourceDownloader) {

    override fun supportedDomains(): List<String> {
        return singletonList("bonprix.de")
    }

    override fun parseExternalId(document: Document): String {
        return document.select("div#product-page").attr("data-product-ordernumber")
    }

    override fun parseTitle(document: Document): String {
        return document.select("h1.product-name").text()
    }

    override fun parseDescription(document: Document): String {
        return document.select("p.product-information-full-description").text()
    }

    override fun parseCurrentPrice(document: Document): BigDecimal {
        val priceAsString = document.select("div#offer > span.price").attr("content")
        return BigDecimal(priceAsString)
    }

    override fun parseCurrencyCode(url: String, document: Document): String {
        return "EUR"
    }

    override fun parseDiscount(currentPrice: BigDecimal, document: Document): Optional<Discount> {
        var discount: Discount? = null

        val formerPrice = document.select("span.price.former-price").text()

        if (!formerPrice.isEmpty()) {
            val oldPriceAsString = formerPrice.replace("€", "").replace(",", ".")

            discount = Discount.of(BigDecimal(oldPriceAsString), currentPrice)
        }

        return Optional.ofNullable(discount)
    }

    override fun parseImages(document: Document): List<Image> {
        val imageWrapperElements = document.select("div#carousel_product_look div.image-wrapper")

        val images = ArrayList<Image>()

        imageWrapperElements.forEach({ element ->
            val image = Image()

            val dataImageSrc = element.attr("data-image-src")
            val dataZoomImageSrc = element.attr("data-zoom-image-src")
            val dataPreviewImageSrc = element.attr("data-preview-image-src")

            image.addVariant(buildImageVariantFromUrl(dataImageSrc))
            image.addVariant(buildImageVariantFromUrl(dataZoomImageSrc))
            image.addVariant(buildImageVariantFromUrl(dataPreviewImageSrc))

            images.add(image)
        })

        return images
    }

    private fun buildImageVariantFromUrl(imageSource: String): ImageVariant {
        val matcher = PATTERN_IMG_DIMENSIONS.matcher(imageSource)
        if (matcher.find()) {
            val height = Integer.parseInt(matcher.group(1))
            val width = Integer.parseInt(matcher.group(2))

            return ImageVariant(
                    url = IMAGE_PREFIX + imageSource,
                    height = height,
                    width = width)
        }

        throw ParserException("Error parsing images with url " + imageSource)
    }

    companion object {

        private val PATTERN_IMG_DIMENSIONS = Pattern.compile("""\/(\d+)x(\d+)\/""")

        private val IMAGE_PREFIX = "https:"
    }

}
