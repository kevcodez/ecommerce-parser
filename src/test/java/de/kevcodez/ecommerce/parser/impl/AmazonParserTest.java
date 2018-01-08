package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.kevcodez.ecommerce.parser.domain.image.Image;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class AmazonParserTest extends AbstractParserTest {

    private static final String SAMPLE_GAME_OF_THRONES_S7 = "/amazon/sample_game_of_thrones_s7";
    private static final String SAMPLE_DISCOUNTED = "/amazon/sample_discounted";

    private static final String VALID_AMAZON_DE_URL = "https://www.amazon.de/gp/product/B002OLT9R8";

    private AmazonParser amazonParser;

    @BeforeEach
    void setup() {
        amazonParser = new AmazonParser(websiteSourceDownloader);
    }

    @Test
    void parseSampleGamesOfThronesS7() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_GAME_OF_THRONES_S7));

        Product product = amazonParser.parse(VALID_AMAZON_DE_URL);

        // Base data
        assertAll("Base Data",
            () -> assertThat(product.getTitle()).isEqualTo("Game of Thrones: Die komplette 7. Staffel [Blu-ray]"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_AMAZON_DE_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("B0743DGBT8")
        );

        // Price
        Price price = product.getPrice();

        assertAll("Price",
            () -> assertThat(price.getCurrentPrice()).isCloseTo(new BigDecimal("29.99"), withinPercentage(0.1D)),
            () -> assertThat(price.getCurrency().getCurrencyCode()).isEqualTo("EUR"),
            () -> assertThat(price.getDiscount()).isNull());

        // Images
        assertThat(product.getImages()).hasSize(3);

        Image firstImage = product.getImages().get(0);
        assertThat(firstImage.getVariants()).hasSize(5);

        ImageVariant firstImageVariant = firstImage.getVariants().get(0);
        verifyImageVariant(firstImageVariant, ImageVariant.builder()
            .url("https://images-na.ssl-images-amazon.com/images/I/81AizGC%2BCeL._SX342_.jpg")
            .height(432)
            .width(342)
            .build());

        ImageVariant secondImageVariant = firstImage.getVariants().get(1);
        verifyImageVariant(secondImageVariant, ImageVariant.builder()
            .url("https://images-na.ssl-images-amazon.com/images/I/81AizGC%2BCeL._SX385_.jpg")
            .height(486)
            .width(385)
            .build());
    }

    @Test
    void parseSampleDiscounted() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = amazonParser.parse(VALID_AMAZON_DE_URL);
        Price price = product.getPrice();

        assertThat(price.getCurrentPrice()).isCloseTo(new BigDecimal("12.99"), withinPercentage(0.1D));

        Discount discount = price.getDiscount();
        Discount expectedDiscount = new Discount().setOldPrice(new BigDecimal(25.99))
            .setDiscount(new BigDecimal("13.00"))
            .setPercentage(new BigDecimal("50"));

        verifyDiscount(discount, expectedDiscount);
    }

    @ParameterizedTest
    @MethodSource("internationalUrls")
    void parseCurrencyCode(String url, String expectedCurrencySymbolic) {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = amazonParser.parse(url);
        Currency currency = product.getPrice().getCurrency();
        assertThat(currency).isNotNull();
        assertThat(currency.getCurrencyCode()).isEqualTo(expectedCurrencySymbolic);
    }

    private static Stream<Arguments> internationalUrls() {
        return Stream.of(
            Arguments.of("https://amazon.de/123", "EUR"),
            Arguments.of("http://amazon.com/123", "USD"));
    }

    @ParameterizedTest
    @MethodSource("dataUrls")
    void matches(String url, boolean shouldMatch) {
        assertThat(amazonParser.matches(url)).isEqualTo(shouldMatch);
    }

    private static Stream<Arguments> dataUrls() {
        return Stream.of(
            Arguments.of("amazon.de", true),
            Arguments.of("amazon.com", true),
            Arguments.of("amazona.de", false),
            Arguments.of("amazon.foo", false)
        );
    }

}
