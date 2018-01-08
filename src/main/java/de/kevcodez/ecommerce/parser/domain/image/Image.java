package de.kevcodez.ecommerce.parser.domain.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
public class Image {

    private final List<ImageVariant> variants = new ArrayList<>();

    public Image() {
    }

    public Image addVariant(ImageVariant imageVariant) {
        variants.add(imageVariant);
        return this;
    }

    public List<ImageVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

}
