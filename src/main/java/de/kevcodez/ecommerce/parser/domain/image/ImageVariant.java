package de.kevcodez.ecommerce.parser.domain.image;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageVariant {

    private String url;

    private int height;

    private int width;

}
