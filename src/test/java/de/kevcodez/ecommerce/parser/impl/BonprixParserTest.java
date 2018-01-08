package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.image.ImageVariant;
import de.kevcodez.ecommerce.parser.domain.price.Discount;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class BonprixParserTest extends AbstractParserTest {

    private static final String SAMPLE_REGULAR = "/bonprix/sample_regular";
    private static final String SAMPLE_DISCOUNTED = "/bonprix/sample_discounted";

    private static final String VALID_URL = "https://www.bonprix.de/produkt/jeans-straight-blau-905358/";

    private BonPrixParser bonPrixParser;

    @BeforeEach
    void setup() {
        bonPrixParser = new BonPrixParser(websiteSourceDownloader);
    }

    @Test
    void baseDataRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = bonPrixParser.parse(VALID_URL);

        // Base data
        assertAll("Base Data",
            () -> assertThat(product.getTitle()).isEqualTo("Jeans Regular Fit Straight"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("90535895"),
            () -> assertThat(product.getDescription()).startsWith("Diese Herren Jeans Regular Fit von John Baner")
        );

        assertThat(product.getPrice().getCurrentPrice()).isCloseTo(new BigDecimal("19.99"), withinPercentage(0.1D));
    }

    @Test
    void imagesRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = bonPrixParser.parse(VALID_URL);

        List<ImageDto> images = product.getImages();
        assertThat(images).hasSize(7);

        ImageDto firstImage = images.get(0);

        assertThat(firstImage.getVariants()).hasSize(3);

        assertThat(firstImage.getVariants()).contains(ImageVariant.builder()
            .url("https://image01.bonprix.de/assets/319x448/1511338838/15045955-hDEd0CBM.jpg")
            .height(319)
            .width(448)
            .build());
        assertThat(firstImage.getVariants()).contains(ImageVariant.builder()
            .url("https://image01.bonprix.de/assets/31x44/1511338838/15045955-hDEd0CBM.jpg")
            .height(31)
            .width(44)
            .build());
        assertThat(firstImage.getVariants()).contains(ImageVariant.builder()
            .url("https://image01.bonprix.de/assets/957x1344/1511338838/15045955-hDEd0CBM.jpg")
            .height(957)
            .width(1344)
            .build());
    }

    @Test
    void sampleDiscount() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = bonPrixParser.parse(VALID_URL);
        Price price = product.getPrice();

        assertThat(price.getCurrentPrice()).isCloseTo(new BigDecimal("9.99"), withinPercentage(0.1D));

        Discount discount = price.getDiscount();
        Discount expectedDiscount = new Discount()
            .setDiscount(BigDecimal.valueOf(3L))
            .setOldPrice(new BigDecimal("12.99"))
            .setPercentage(BigDecimal.valueOf(23.09D));

        verifyDiscount(discount, expectedDiscount);
    }

}
