package de.kevcodez.ecommerce.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
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
import de.kevcodez.ecommerce.parser.domain.price.Price;
import de.kevcodez.ecommerce.parser.domain.product.Product;

class ConradParserTest extends AbstractParserTest {

    private static final String SAMPLE_REGULAR = "/conrad/sample_regular";
    private static final String SAMPLE_DISCOUNTED = "/conrad/sample_discounted";

    private static final String VALID_URL = "https://www.conrad.de/de/holzduebel-wolfcraft-40-mm-10-mm-2910000-30-st-484626.html";

    private ConradParser conradParser;

    @BeforeEach
    void setup() {
        conradParser = new ConradParser(websiteSourceDownloader);
    }

    @Test
    void baseDataRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = conradParser.parse(VALID_URL);

        // Base data
        assertAll("Base Data",
            () -> assertThat(product.getTitle()).isEqualTo("Raspberry Pi® 3 Model B Advanced-Set 1 GB"),
            () -> assertThat(product.getUrl()).isEqualTo(VALID_URL),
            () -> assertThat(product.getExternalId()).isEqualTo("1419717"),
            () -> assertThat(product.getDescription())
                .startsWith("Der Raspberry Pi® 3 ist die leistungsstarke Weiterentwicklung")
        );

        assertThat(product.getPrice().getCurrentPrice()).isCloseTo(new BigDecimal("84.99"), withinPercentage(0.1D));
    }

    @Test
    void sampleDiscount() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_DISCOUNTED));

        Product product = conradParser.parse(VALID_URL);
        Price price = product.getPrice();

        assertThat(price.getCurrentPrice()).isCloseTo(new BigDecimal("40.99"), withinPercentage(0.1D));

        Discount discount = price.getDiscount();
        Discount expectedDiscount = new Discount()
            .setOldPrice(new BigDecimal("54.99"))
            .setDiscount(BigDecimal.valueOf(14L))
            .setPercentage(BigDecimal.valueOf(25.46D));

        verifyDiscount(discount, expectedDiscount);
    }

    @Test
    void imagesRegular() {
        when(websiteSourceDownloader.download(anyString())).thenReturn(getResourceAsString(SAMPLE_REGULAR));

        Product product = conradParser.parse(VALID_URL);
        List<Image> images = product.getImages();

        assertThat(images).hasSize(4);

        Image firstImage = images.get(0);
        List<ImageVariant> firstImageVariants = firstImage.getVariants();
        assertThat(firstImageVariants).hasSize(2);

        assertThat(firstImageVariants).contains(ImageVariant.builder()
            .url(
                "https://asset.conrad.com/media10/isa/160267/c1/-/de/1419717_GB_01_FB/raspberry-pi-3-model-b-advanced-set-1-gb.jpg?x=520&y=520")
            .width(520)
            .height(520)
            .build());

        assertThat(firstImageVariants).contains(ImageVariant.builder()
            .url(
                "https://asset.conrad.com/media10/isa/160267/c1/-/de/1419717_GB_01_FB/raspberry-pi-3-model-b-advanced-set-1-gb.jpg?x=76&y=76")
            .width(76)
            .height(76)
            .build());
    }

}
