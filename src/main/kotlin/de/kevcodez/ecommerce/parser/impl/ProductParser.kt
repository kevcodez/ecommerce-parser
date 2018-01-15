package de.kevcodez.ecommerce.parser.impl

import de.kevcodez.ecommerce.parser.domain.product.Product

interface ProductParser {

    fun parse(url: String): Product

    fun matches(domain: String): Boolean

}
