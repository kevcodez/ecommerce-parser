package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class AlternateParserTest extends AbstractParserTest {

    private static final String SAMPLE_AMD_RYZEN = "/alternate/sample_AMD_Ryzen";
    private static final String SAMPLE_MX300_SSD = "/alternate/sample_MX300_SSD";

    private static final String VALID_URL = "https://www.alternate.de/AMD/Ryzen-5-1400-WRAITH-Prozessor/html/product/1340575";

    private AlternateParser alternateParser;

    @BeforeEach
    void setup() {
        alternateParser = new AlternateParser(websiteSourceDownloader);
    }

    @Test
    void parseSampleAMDRyzen() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_AMD_RYZEN));

        Product product = alternateParser.parse(VALID_URL);

        assertAll("Base data",
            () -> assertThat(product.getTitle()).isEqualTo("AMD Ryzen 5 1400 WRAITH, Prozessor"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1340575"),
            () -> assertThat(product.getDescription()).isEqualTo(
                "Der AMD Ryzen 5 1400 Processor ist eine Quad-Core-CPU für den Sockel AM4 mit 3,2 GHz Taktfrequenz und 8 Mbyte L3-Cache.")
        );

        //        ImageDto image = dataDto.getImage();
        //        assertAll("image",
        //            () -> assertThat(image.getUrl())
        //                .isEqualTo("https://www.alternate.de/p/230x230/h/AMD_Ryzen_5_1400_WRAITH__Prozessor@@hr5a01.jpg"),
        //            () -> assertThat(image.getHeight()).isEqualTo(230),
        //            () -> assertThat(image.getWidth()).isEqualTo(230));
    }

    @Test
    void parseSampleMX300SSD() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_MX300_SSD));

        Product product = alternateParser.parse(VALID_URL);

        assertAll("Base data",
            () -> assertThat(product.getTitle()).isEqualTo("Crucial MX300 525 GB, Solid State Drive"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1289011")
        );

        Discount discount = product.getPrice().getDiscount();
        assertAll("Discount",
            () -> assertThat(discount.getValue()).isCloseTo(BigDecimal.valueOf(2L), withPercentage(0.1)),
            () -> assertThat(discount.getPercentage()).isCloseTo(BigDecimal.valueOf(1.48F), withPercentage(0.1)));

        //        ImageDto image = dataDto.getImage();
        //        assertAll("image",
        //            () -> assertThat(image.getUrl())
        //                .isEqualTo(
        //                    "https://www.alternate.de/p/230x230/i/Crucial_MX300_525_GB__Solid_State_Drive@@imkmcs0_30.jpg"),
        //            () -> assertThat(image.getHeight()).isEqualTo(230),
        //            () -> assertThat(image.getWidth()).isEqualTo(230));
    }

}
