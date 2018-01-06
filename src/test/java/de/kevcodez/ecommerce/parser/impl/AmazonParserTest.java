package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class AmazonParserTest extends AbstractParserTest {

    private static final String SAMPLE_GAME_OF_THRONES_S7 = "/amazon/sample_game_of_thrones_s7";
    private static final String SAMPLE_YELLOW_DUCK = "/amazon/sample_yellow_duck";
    private static final String SAMPLE_AUTO_RUECKLEHNE = "/amazon/sample_auto_ruecklehne";

    private static final String VALID_AMAZON_URL = "https://www.amazon.de/gp/product/B002OLT9R8";

    private AmazonParser amazonParser;

    @BeforeEach
    void setup() {
        amazonParser = new AmazonParser(websiteSourceDownloader);
    }

    @Test
    void parseSampleGamesOfThronesS7() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_GAME_OF_THRONES_S7));

        Product product = amazonParser.parse(VALID_AMAZON_URL);

        // Base data
        assertAll("Base Data",
            () -> assertThat(product.getTitle()).isEqualTo("Game of Thrones: Die komplette 7. Staffel [Blu-ray]"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_AMAZON_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("B0743DGBT8")
        );

        // Price
        Price price = product.getPrice();

        assertAll("Price",
            () -> assertThat(price.getCurrentPrice())
                .isCloseTo(new BigDecimal("29.99"), within(BigDecimal.valueOf(0.001))),
            () -> assertThat(price.getCurrency().getCurrencyCode()).isEqualTo("EUR"),
            () -> assertThat(price.getDiscount()).isNull());

        // Images
        assertThat(product.getImages().size()).isEqualTo(3);

        ImageDto firstImage = product.getImages().get(0);
        assertThat(firstImage.getVariants().size()).isEqualTo(5);

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
    void parseSampleYellowDuck() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_YELLOW_DUCK));

        Product product = amazonParser.parse(VALID_AMAZON_URL);

        assertAll("Base Data",
            () -> assertThat(product.getTitle())
                .isEqualTo("gelbe Ente Schlüsselanhänger Figur mit LED und Sound \"NaagNaagNag\" Ente"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_AMAZON_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("B01MYE2CZS")
        );

        Price price = product.getPrice();
        assertAll("Price",
            () -> assertThat(price.getCurrentPrice()).isEqualTo(new BigDecimal("8.90")));

        List<ImageDto> images = product.getImages();
        assertThat(images.size()).isEqualTo(3);

        ImageDto firstImage = product.getImages().get(0);
        assertThat(firstImage.getVariants().size()).isEqualTo(5);

        ImageVariant firstImageVariant = firstImage.getVariants().get(0);
        verifyImageVariant(firstImageVariant, ImageVariant.builder()
            .url("https://images-na.ssl-images-amazon.com/images/I/31XbLyqykGL._SY355_.jpg")
            .height(355)
            .width(354)
            .build());

        ImageVariant secondImageVariant = firstImage.getVariants().get(1);
        verifyImageVariant(secondImageVariant, ImageVariant.builder()
            .url("https://images-na.ssl-images-amazon.com/images/I/31XbLyqykGL._SY450_.jpg")
            .height(450)
            .width(449)
            .build());
    }

    @Test
    void parseSampleAutoRuecklehne() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_AUTO_RUECKLEHNE));

        Product product = amazonParser.parse(VALID_AMAZON_URL);
        Price price = product.getPrice();
        assertAll("Price",
            () -> assertThat(price.getCurrentPrice()).isEqualTo(new BigDecimal("12.99")),
            () -> assertThat(price.getCurrency().getCurrencyCode()).isEqualTo("EUR"));

        Discount discount = price.getDiscount();
        assertAll("Discount",
            () -> assertThat(discount.getValue()).isEqualTo(new BigDecimal("13.00")),
            () -> assertThat(discount.getPercentage()).isEqualTo(new BigDecimal("50")));
    }

    @ParameterizedTest
    @MethodSource("dataUrls")
    void matches(String url, boolean shouldMatch) {
        assertThat(amazonParser.matches(url)).isEqualTo(shouldMatch);
    }

    private static Stream<Arguments> dataUrls() {
        return Stream.of(
            Arguments.of("https://amazon.de", true),
            Arguments.of("http://amazon.de", true),
            Arguments.of("www.amazon.de", true),
            Arguments.of("https://amazon.com", true),
            Arguments.of("http://amazon.com", true),
            Arguments.of("www.amazon.com", true),
            Arguments.of("https://www.amazon.com", true),
            Arguments.of("http://www.amazon.com", true),
            Arguments.of("http://amazon-foo.de", false)
        );
    }

}
