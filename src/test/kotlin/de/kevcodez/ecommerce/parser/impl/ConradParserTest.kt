package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.whenever
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withinPercentage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import java.math.BigDecimal

internal class ConradParserTest : AbstractParserTest() {

    private lateinit var conradParser: ConradParser

    @BeforeEach
    fun setup() {
        conradParser = ConradParser(websiteSourceDownloader)
    }

    @Test
    fun baseDataRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = conradParser.parse(VALID_URL)

        // Base data
        assertThat(product.title).isEqualTo("Raspberry Pi® 3 Model B Advanced-Set 1 GB")
        assertThat(product.url).isEqualTo(VALID_URL)
        assertThat(product.externalId).isEqualTo("1419717")

        assertThat(product.description).startsWith("Der Raspberry Pi® 3 ist die leistungsstarke Weiterentwicklung")


        assertThat(product.price!!.currentPrice).isCloseTo(BigDecimal("84.99"), withinPercentage(0.1))
    }

    @Test
    fun sampleDiscount() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = conradParser.parse(VALID_URL)
        val price = product.price!!

        assertThat(price.currentPrice).isCloseTo(BigDecimal("40.99"), withinPercentage(0.1))

        val discount = price.discount!!
        val expectedDiscount = Discount(oldPrice = BigDecimal("54.99"), discount = BigDecimal.valueOf(14L), percentage = BigDecimal.valueOf(25.46))

        verifyDiscount(discount, expectedDiscount)
    }

    @Test
    fun imagesRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = conradParser.parse(VALID_URL)
        val images = product.images

        assertThat(images).hasSize(4)

        val firstImage = images[0]
        val firstImageVariants = firstImage.variants
        assertThat(firstImageVariants).hasSize(2)

        assertThat(firstImageVariants).contains(ImageVariant(
                url = "https://asset.conrad.com/media10/isa/160267/c1/-/de/1419717_GB_01_FB/raspberry-pi-3-model-b-advanced-set-1-gb.jpg?x=520&y=520",
                width = 520,
                height = 520))

        assertThat(firstImageVariants).contains(ImageVariant(
                url = "https://asset.conrad.com/media10/isa/160267/c1/-/de/1419717_GB_01_FB/raspberry-pi-3-model-b-advanced-set-1-gb.jpg?x=76&y=76",
                width = 76,
                height = 76))
    }

    companion object {

        private val SAMPLE_REGULAR = "/conrad/sample_regular"
        private val SAMPLE_DISCOUNTED = "/conrad/sample_discounted"

        private val VALID_URL = "https://www.conrad.de/de/holzduebel-wolfcraft-40-mm-10-mm-2910000-30-st-484626.html"
    }

}
