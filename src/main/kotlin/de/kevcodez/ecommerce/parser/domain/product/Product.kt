package de.kevcodez.ecommerce.parser.domain.product

import de.kevcodez.ecommerce.parser.domain.image.Image
import de.kevcodez.ecommerce.parser.domain.price.Price

data class Product(
        val url: String?,
        val externalId: String?,
        val title: String?,
        val description: String?,
        val price: Price?,
        val images: MutableList<Image>)