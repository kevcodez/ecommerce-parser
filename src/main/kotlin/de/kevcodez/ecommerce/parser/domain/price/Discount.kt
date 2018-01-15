package de.kevcodez.ecommerce.parser.domain.price

import java.math.BigDecimal
import java.math.RoundingMode

data class Discount(var oldPrice: BigDecimal?, var discount: BigDecimal?, var percentage: BigDecimal?) {

    companion object {

        fun of(oldPrice: BigDecimal, newPrice: BigDecimal): Discount {
            val discount = oldPrice.subtract(newPrice)
            val percentage = discount.divide(oldPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100L))
                    .setScale(2, RoundingMode.HALF_UP)

            return Discount(oldPrice, discount, percentage)
        }
    }
}
