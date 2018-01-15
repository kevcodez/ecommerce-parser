package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.whenever
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.withinPercentage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import java.math.BigDecimal

internal class BonprixParserTest : AbstractParserTest() {

    private lateinit var bonPrixParser: BonPrixParser

    @BeforeEach
    fun setup() {
        bonPrixParser = BonPrixParser(websiteSourceDownloader)
    }

    @Test
    fun baseDataRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = bonPrixParser.parse(VALID_URL)

        // Base data
        assertThat(product.title).isEqualTo("Jeans Regular Fit Straight")
        assertThat(product.url).isEqualTo(VALID_URL)
        assertThat(product.externalId).isEqualTo("90535895")
        assertThat(product.description).startsWith("Diese Herren Jeans Regular Fit von John Baner")

        assertThat(product.price!!.currentPrice).isCloseTo(BigDecimal("19.99"), withinPercentage(0.1))
    }

    @Test
    fun imagesRegular() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR))

        val product = bonPrixParser.parse(VALID_URL)

        val images = product.images
        assertThat(images).hasSize(7)

        val firstImage = images[0]

        assertThat(firstImage.variants).hasSize(3)

        assertThat(firstImage.variants).contains(ImageVariant(
                url = "https://image01.bonprix.de/assets/319x448/1511338838/15045955-hDEd0CBM.jpg",
                height = 319,
                width = 448))

        assertThat(firstImage.variants).contains(ImageVariant(
                url = "https://image01.bonprix.de/assets/31x44/1511338838/15045955-hDEd0CBM.jpg",
                height = 31,
                width = 44))

        assertThat(firstImage.variants).contains(ImageVariant(
                url = "https://image01.bonprix.de/assets/957x1344/1511338838/15045955-hDEd0CBM.jpg",
                height = 957,
                width = 1344))
    }

    @Test
    fun sampleDiscount() {
        whenever(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED))

        val product = bonPrixParser.parse(VALID_URL)
        val price = product.price!!

        assertThat(price.currentPrice).isCloseTo(BigDecimal("9.99"), withinPercentage(0.1))

        val discount = price.discount!!
        val expectedDiscount = Discount(discount = BigDecimal.valueOf(3L), oldPrice = BigDecimal("12.99"), percentage = BigDecimal.valueOf(23.09))

        verifyDiscount(discount, expectedDiscount)
    }

    companion object {

        private val SAMPLE_REGULAR = "/bonprix/sample_regular"
        private val SAMPLE_DISCOUNTED = "/bonprix/sample_discounted"

        private val VALID_URL = "https://www.bonprix.de/produkt/jeans-straight-blau-905358/"
    }

}
