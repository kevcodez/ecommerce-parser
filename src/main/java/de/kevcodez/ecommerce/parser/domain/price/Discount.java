package de.kevcodez.ecommerce.parser.domain.price;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;

@Getter
public class Discount {

    private BigDecimal oldPrice;

    private BigDecimal value;

    private BigDecimal percentage;

    public Discount(BigDecimal discount, BigDecimal percentage) {
        this.value = discount;
        this.percentage = percentage;
    }

    public static Discount of(BigDecimal oldPrice, BigDecimal newPrice) {
        BigDecimal discount = oldPrice.subtract(newPrice);
        BigDecimal percentage = discount.divide(oldPrice, 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100L));

        return new Discount(discount, percentage);
    }

}
