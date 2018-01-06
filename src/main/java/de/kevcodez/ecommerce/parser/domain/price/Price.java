package de.kevcodez.ecommerce.parser.domain.price;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Getter;

@Getter
public class Price {

    private final BigDecimal currentPrice;

    private final Currency currency;

    private final Discount discount;

    public Price(BigDecimal currentPrice, String currencyCode, Discount discount) {
        this.currentPrice = currentPrice;
        this.currency = Currency.getInstance(currencyCode);
        this.discount = discount;
    }

}
