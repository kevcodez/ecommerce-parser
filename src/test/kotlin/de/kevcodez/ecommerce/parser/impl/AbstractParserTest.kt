package de.kevcodez.ecommerce.parser.impl

import com.nhaarman.mockito_kotlin.mock
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant
import de.kevcodez.ecommerce.parser.domain.price.Discount
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import org.assertj.core.api.Assertions.withinPercentage
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach

const val RESOURCE_PREFIX = "/data"

internal open class AbstractParserTest {

    lateinit var websiteSourceDownloader: WebsiteSourceDownloader

    @BeforeEach
    fun mockWebsiteSourceDownloader() {
        websiteSourceDownloader = mock()
    }

    fun getResourceAsString(resource: String): String {
        return AbstractParserTest::class.java.getResource(RESOURCE_PREFIX + resource).readText()
    }

    fun verifyImageVariant(variant: ImageVariant, expected: ImageVariant) {
        assertThat(variant.url).isEqualTo(expected.url)
        assertThat(variant.height).isEqualTo(expected.height)
        assertThat(variant.width).isEqualTo(expected.width)
    }

    fun verifyDiscount(discount: Discount, expectedDiscount: Discount) {
        assertThat(discount.oldPrice).isCloseTo(expectedDiscount.oldPrice, withinPercentage(0.1))
        assertThat(discount.discount).isCloseTo(expectedDiscount.discount, withinPercentage(0.1))
        assertThat(discount.percentage).isCloseTo(expectedDiscount.percentage, withinPercentage(0.1))
    }

}
