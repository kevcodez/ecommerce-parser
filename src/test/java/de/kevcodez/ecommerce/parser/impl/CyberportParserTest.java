package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class CyberportParserTest extends AbstractParserTest {

    private static final String SAMPLE_REGULAR = "/cyberport/sample_regular";
    private static final String SAMPLE_DISCOUNTED = "/cyberport/sample_discounted";

    private static final String VALID_URL = "https://www.cyberport.de/apple-macbook-pro-13-3-retina-2017-i5-2-3-8-128-gb-iip640-space-grau-mpxq2d-a-1A09-0AY_8465.html";

    private CyberportParser cyberportParser;

    @BeforeEach
    void setup() {
        cyberportParser = new CyberportParser(websiteSourceDownloader);
    }

    @Test
    void baseDataRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = cyberportParser.parse(VALID_URL);

        // Base data
        assertAll("Base Data",
            () -> assertThat(product.getTitle()).isEqualTo("Apple MacBook Pro 13,3\" Retina 2017 i5 2,3/8/128 GB IIP640 Space Grau MPXQ2D/A"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1A09-0AY_8465"),
            () -> assertThat(product.getDescription())
                .startsWith("Es ist schneller und leistungsstärker")
        );

        assertThat(product.getPrice().getCurrentPrice()).isCloseTo(new BigDecimal("1333.00"), withinPercentage(0.1D));
    }

    @Test
    void imagesRegular() {
        // TODO
    }

    @Test
    void sampleDiscount() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = cyberportParser.parse(VALID_URL);
        Price price = product.getPrice();

        assertThat(price.getCurrentPrice()).isCloseTo(new BigDecimal("89.90"), withinPercentage(0.1D));

        Discount discount = price.getDiscount();
        Discount expectedDiscount = new Discount()
            .setOldPrice(new BigDecimal("109.00"))
            .setDiscount(BigDecimal.valueOf(19.1D))
            .setPercentage(BigDecimal.valueOf(17.52D));

        verifyDiscount(discount, expectedDiscount);
    }

}
