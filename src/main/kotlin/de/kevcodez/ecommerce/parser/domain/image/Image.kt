package de.kevcodez.ecommerce.parser.domain.image

data class Image(val variants: ArrayList<ImageVariant>) {

    constructor() : this(ArrayList())

    fun addVariant(imageVariant: ImageVariant): Image {
        this.variants.add(imageVariant)
        return this
    }

}
