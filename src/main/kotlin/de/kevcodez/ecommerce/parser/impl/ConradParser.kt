package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import de.kevcodez.ecommerce.parser.exception.ParserException
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.util.*
import java.util.Arrays.asList
import java.util.regex.Pattern
import java.util.stream.Collectors

class ConradParser(websiteSourceDownloader: WebsiteSourceDownloader) : JsoupProductParser(websiteSourceDownloader) {

    override fun supportedDomains(): List<String> {
        return asList("conrad.de", "conrad.it")
    }

    override fun parseExternalId(document: Document): String {
        return document.select("span[itemprop='sku']").text()
    }

    override fun parseTitle(document: Document): String {
        return document.select("h1.ccpProductDetail__title__text").text()
    }

    override fun parseDescription(document: Document): String {
        val descriptionSection = document.select("div#description > section")[0]
        return descriptionSection.textNodes().stream()
                .filter({ node -> !node.isBlank })
                .map({ it -> it.wholeText })
                .collect(Collectors.joining("\n"))
                .trim()
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

        val discountAsText = document.select("div.ccpProductDetailInfo__cell__price__old__value > span").text()

        if (!discountAsText.isEmpty()) {
            val oldPriceAsString = discountAsText.replace(" €", "").replace(",", ".")
            discount = Discount.of(BigDecimal(oldPriceAsString), currentPrice)
        }

        return Optional.ofNullable(discount)
    }

    override fun parseImages(document: Document): List<Image> {
        val imageElements = document.select("img.ccpProductDetailSlideshow__slider__wrapper__list__item__image")

        val images = ArrayList<Image>()

        imageElements.forEach({ element ->
            val url = element.attr("src")

            val image = Image()
            val variants = findAllImageUrls(document, url)

            variants.stream()
                    .map({ buildByUrl(it) })
                    .forEach({ image.addVariant(it) })

            images.add(image)
        })

        return images
    }

    private fun findAllImageUrls(document: Document, imageUrl: String): Set<String> {
        val urlWithoutParameters = imageUrl.split("?")[0]

        val urls = HashSet<String>()

        val imgElements = document.select("img")
        imgElements.stream().map({ element -> element.attr("src") })
                .filter({ src -> src.startsWith(urlWithoutParameters) })
                .forEach({ urls.add(it) })

        return urls
    }

    private fun buildByUrl(url: String): ImageVariant {
        val matcher = PATTERN_IMG_DIMENSIONS.matcher(url)

        if (matcher.find()) {
            return ImageVariant(
                    url = url,
                    height = Integer.parseInt(matcher.group(2)),
                    width = Integer.parseInt(matcher.group(1)))
        }

        throw ParserException("Error parsing image variant " + url)
    }

    companion object {

        private val PATTERN_IMG_DIMENSIONS = Pattern.compile("""\?x=(\d+)&y=(\d+)""")
    }
}
