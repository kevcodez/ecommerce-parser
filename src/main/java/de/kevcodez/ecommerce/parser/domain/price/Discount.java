package de.kevcodez.ecommerce.parser.domain.price;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;

@Getter
public class Discount {

    private BigDecimal oldPrice;

    private BigDecimal discount;

    private BigDecimal percentage;

    public static Discount of(BigDecimal oldPrice, BigDecimal newPrice) {
        BigDecimal discount = oldPrice.subtract(newPrice);
        BigDecimal percentage = discount.divide(oldPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100L))
            .setScale(2, RoundingMode.HALF_UP);

        return new Discount()
            .setOldPrice(oldPrice)
            .setPercentage(percentage)
            .setDiscount(discount);
    }

    public Discount setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;

        return this;
    }

    public Discount setDiscount(BigDecimal discount) {
        this.discount = discount;

        return this;
    }

    public Discount setPercentage(BigDecimal percentage) {
        this.percentage = percentage;

        return this;
    }
}
