package de.kevcodez.ecommerce.parser.domain.price

import java.math.BigDecimal
import java.util.*

data class Price(val currentPrice: BigDecimal?, val currency: Currency?, val discount: Discount?)

