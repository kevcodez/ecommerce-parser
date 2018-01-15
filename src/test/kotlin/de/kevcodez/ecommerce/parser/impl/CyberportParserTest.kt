package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.whenever
import de.kevcodez.ecommerce.parser.domain.price.Discount
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withinPercentage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import java.math.BigDecimal

internal class CyberportParserTest : AbstractParserTest() {

    private lateinit var cyberportParser: CyberportParser

    @BeforeEach
    fun setup() {
        cyberportParser = CyberportParser(websiteSourceDownloader)
    }

    @Test
    fun baseDataRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = cyberportParser.parse(VALID_URL)

        // Base data
        assertThat(product.title).isEqualTo("Apple MacBook Pro 13,3\" Retina 2017 i5 2,3/8/128 GB IIP640 Space Grau MPXQ2D/A")
        assertThat(product.url).isEqualTo(VALID_URL)
        assertThat(product.externalId).isEqualTo("1A09-0AY_8465")
        assertThat(product.description).startsWith("Es ist schneller und leistungsstärker")


        assertThat(product.price!!.currentPrice).isCloseTo(BigDecimal("1333.00"), withinPercentage(0.1))
    }

    @Test
    fun imagesRegular() {
        // TODO
    }

    @Test
    fun sampleDiscount() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = cyberportParser.parse(VALID_URL)
        val price = product.price!!

        assertThat(price.currentPrice).isCloseTo(BigDecimal("89.90"), withinPercentage(0.1))

        val discount = price.discount!!
        val expectedDiscount = Discount(oldPrice = BigDecimal("109.00"), discount = BigDecimal.valueOf(19.1), percentage = BigDecimal.valueOf(17.52))

        verifyDiscount(discount, expectedDiscount)
    }

    companion object {

        private val SAMPLE_REGULAR = "/cyberport/sample_regular"
        private val SAMPLE_DISCOUNTED = "/cyberport/sample_discounted"

        private val VALID_URL = "https://www.cyberport.de/apple-macbook-pro-13-3-retina-2017-i5-2-3-8-128-gb-iip640-space-grau-mpxq2d-a-1A09-0AY_8465.html"
    }

}
