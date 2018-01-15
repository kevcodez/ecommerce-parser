package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.whenever
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import java.math.BigDecimal

internal class AlternateParserTest : AbstractParserTest() {

    private lateinit var alternateParser: AlternateParser

    @BeforeEach
    fun setup() {
        alternateParser = AlternateParser(websiteSourceDownloader)
    }

    @Test
    fun parseSampleRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = alternateParser.parse(VALID_URL)

        assertThat(product.title).isEqualTo("AMD Ryzen 5 1400 WRAITH, Prozessor")
        assertThat(product.url).isEqualTo(VALID_URL)
        assertThat(product.externalId).isEqualTo("1340575")
        assertThat(product.description).startsWith("Der AMD Ryzen 5 1400 Processor")

    }

    @Test
    fun parseSampleDiscounted() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = alternateParser.parse(VALID_URL)

        assertThat(product.title).isEqualTo("Crucial MX300 525 GB, Solid State Drive")
        assertThat(product.url).isEqualTo(VALID_URL)
        assertThat(product.externalId).isEqualTo("1289011")

        val discount = product.price!!.discount!!
        val expectedDiscount = Discount(BigDecimal("134.90"), BigDecimal.valueOf(2L), BigDecimal.valueOf(1.48))

        verifyDiscount(discount, expectedDiscount)
    }

    @Test
    fun parseImages() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = alternateParser.parse(VALID_URL)

        val images = product.images
        assertThat(images).hasSize(3)

        val firstImage = images[0]
        val firstImageVariants = firstImage.variants

        assertThat(firstImageVariants).hasSize(2)

        assertThat(firstImageVariants).contains(ImageVariant(
                url = "https://www.alternate.de/p/50x50/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@hr5a01.jpg",
                height = 50,
                width = 50))

        assertThat(firstImageVariants).contains(ImageVariant(
                url = "https://www.alternate.de/p/230x230/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@hr5a01.jpg",
                height = 230,
                width = 230))
    }

    companion object {

        private val SAMPLE_REGULAR = "/alternate/sample_regular"
        private val SAMPLE_DISCOUNTED = "/alternate/sample_discounted"

        private val VALID_URL = "https://www.alternate.de/AMD/Ryzen-5-1400-WRAITH-Prozessor/html/product/1340575"
    }

}
