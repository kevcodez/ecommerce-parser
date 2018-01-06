package de.kevcodez.ecommerce.parser.domain.product;

import java.util.ArrayList;
import java.util.List;

import de.kevcodez.ecommerce.parser.domain.image.ImageDto;
import de.kevcodez.ecommerce.parser.domain.price.Price;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private String url;

    private String externalId;

    private String title;

    private String description;

    private Price price;

    private List<ImageDto> images = new ArrayList<>();

    public Product() {

    }

}
