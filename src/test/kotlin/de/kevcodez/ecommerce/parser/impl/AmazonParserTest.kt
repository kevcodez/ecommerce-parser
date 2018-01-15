package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.whenever
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withinPercentage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import java.math.BigDecimal

internal class AmazonParserTest : AbstractParserTest() {

    private lateinit var amazonParser: AmazonParser

    @BeforeEach
    fun setup() {
        amazonParser = AmazonParser(websiteSourceDownloader)
    }

    @Test
    fun parseSampleGamesOfThronesS7() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_GAME_OF_THRONES_S7))

        val product = amazonParser.parse(VALID_AMAZON_DE_URL)

        // Base data
        assertThat(product.title).isEqualTo("Game of Thrones: Die komplette 7. Staffel [Blu-ray]")
        assertThat(product.url).isEqualTo(VALID_AMAZON_DE_URL)
        assertThat(product.externalId).isEqualTo("B0743DGBT8")

        // Price
        val price = product.price!!

        assertThat(price.currentPrice).isCloseTo(BigDecimal("29.99"), withinPercentage(0.1))
        assertThat(price.currency!!.currencyCode).isEqualTo("EUR")
        assertThat(price.discount).isNull()

        // Images
        assertThat(product.images).hasSize(3)

        val firstImage = product.images[0]
        assertThat(firstImage.variants).hasSize(5)

        val firstImageVariant = firstImage.variants[0]
        verifyImageVariant(firstImageVariant, ImageVariant(
                url = "https://images-na.ssl-images-amazon.com/images/I/81AizGC%2BCeL._SX342_.jpg",
                height = 432,
                width = 342))

        val secondImageVariant = firstImage.variants[1]
        verifyImageVariant(secondImageVariant, ImageVariant(
                url = "https://images-na.ssl-images-amazon.com/images/I/81AizGC%2BCeL._SX385_.jpg",
                height = 486,
                width = 385))
    }

    @Test
    fun parseSampleDiscounted() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = amazonParser.parse(VALID_AMAZON_DE_URL)
        val price = product.price!!

        assertThat(price.currentPrice).isCloseTo(BigDecimal("12.99"), withinPercentage(0.1))

        val discount = price.discount!!
        val expectedDiscount = Discount(oldPrice = BigDecimal(25.99),
                discount = BigDecimal("13.00"),
                percentage = BigDecimal("50"))

        verifyDiscount(discount, expectedDiscount)
    }

    @ParameterizedTest
    @MethodSource("internationalUrls")
    fun parseCurrencyCode(url: String, expectedCurrencySymbolic: String) {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = amazonParser.parse(url)
        val currency = product.price!!.currency
        assertThat(currency).isNotNull()
        assertThat(currency!!.currencyCode).isEqualTo(expectedCurrencySymbolic)
    }

    @ParameterizedTest
    @MethodSource("dataUrls")
    fun matches(url: String, shouldMatch: Boolean) {
        assertThat(amazonParser.matches(url)).isEqualTo(shouldMatch)
    }

    companion object {

        private val SAMPLE_GAME_OF_THRONES_S7 = "/amazon/sample_game_of_thrones_s7"
        private val SAMPLE_DISCOUNTED = "/amazon/sample_discounted"

        private val VALID_AMAZON_DE_URL = "https://www.amazon.de/gp/product/B002OLT9R8"

        @JvmStatic
        fun internationalUrls() = listOf(
                of("https://amazon.de/123", "EUR"),
                of("http://amazon.com/123", "USD"))


        @JvmStatic
        fun dataUrls() = listOf(
                of("amazon.de", true),
                of("amazon.com", true),
                of("amazona.de", false),
                of("amazon.foo", false)
        )
    }

}
