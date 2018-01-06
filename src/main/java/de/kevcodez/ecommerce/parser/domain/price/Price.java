package de.kevcodez.ecommerce.parser.domain.price;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Getter;

@Getter
public class Price {

    private BigDecimal currentPrice;

    private Currency currency;

    private Discount discount;

    public Price() {
    }

    public Price setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;

        return this;
    }

    public Price setDiscount(Discount discount) {
        this.discount = discount;

        return this;
    }

    public Price setCurrency(String currenyCode) {
        this.currency = Currency.getInstance(currenyCode);

        return this;
    }

    public Price setCurrency(Currency currency) {
        this.currency = currency;

        return this;
    }
}
