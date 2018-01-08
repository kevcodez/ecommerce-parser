package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.kevcodez.ecommerce.parser.domain.image.Image;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class AlternateParserTest extends AbstractParserTest {

    private static final String SAMPLE_REGULAR = "/alternate/sample_regular";
    private static final String SAMPLE_DISCOUNTED = "/alternate/sample_discounted";

    private static final String VALID_URL = "https://www.alternate.de/AMD/Ryzen-5-1400-WRAITH-Prozessor/html/product/1340575";

    private AlternateParser alternateParser;

    @BeforeEach
    void setup() {
        alternateParser = new AlternateParser(websiteSourceDownloader);
    }

    @Test
    void parseSampleRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = alternateParser.parse(VALID_URL);

        assertAll("Base data",
            () -> assertThat(product.getTitle()).isEqualTo("AMD Ryzen 5 1400 WRAITH, Prozessor"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1340575"),
            () -> assertThat(product.getDescription()).isEqualTo(
                "Der AMD Ryzen 5 1400 Processor ist eine Quad-Core-CPU für den Sockel AM4 mit 3,2 GHz Taktfrequenz und 8 Mbyte L3-Cache.")
        );
    }

    @Test
    void parseSampleDiscounted() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = alternateParser.parse(VALID_URL);

        assertAll("Base data",
            () -> assertThat(product.getTitle()).isEqualTo("Crucial MX300 525 GB, Solid State Drive"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1289011")
        );

        Discount discount = product.getPrice().getDiscount();
        Discount expectedDiscount = new Discount()
            .setOldPrice(new BigDecimal("134.90"))
            .setDiscount(BigDecimal.valueOf(2L))
            .setPercentage(BigDecimal.valueOf(1.48F));

        verifyDiscount(discount, expectedDiscount);
    }

    @Test
    void parseImages() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = alternateParser.parse(VALID_URL);

        List<Image> images = product.getImages();
        assertThat(images).hasSize(3);

        Image firstImage = images.get(0);
        List<ImageVariant> firstImageVariants = firstImage.getVariants();

        assertThat(firstImageVariants).hasSize(2);

        assertThat(firstImageVariants).contains(ImageVariant.builder()
            .url("https://www.alternate.de/p/50x50/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@hr5a01.jpg")
            .height(50)
            .width(50)
            .build());

        assertThat(firstImageVariants).contains(ImageVariant.builder()
            .url("https://www.alternate.de/p/230x230/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@hr5a01.jpg")
            .height(230)
            .width(230)
            .build());
    }

}
