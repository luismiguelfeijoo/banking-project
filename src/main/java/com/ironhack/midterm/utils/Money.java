package com.ironhack.midterm.utils;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Currency;

@Embeddable
public class Money {
    //private static final Currency USD = Currency.getInstance("USD");
    // For simplicity I use only EUR
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private Currency currency;
    @NotNull
    private BigDecimal amount;
    /**
     * Class constructor specifying amount, currency, and rounding
     **/
    public Money(@NotNull @PositiveOrZero BigDecimal amount, Currency currency, RoundingMode rounding) {
        this.currency = currency;
        setAmount(amount.setScale(currency.getDefaultFractionDigits(), rounding));
    }
    /**
     * Class constructor specifying amount, and currency. Uses default RoundingMode HALF_EVEN.
     **/
    public Money(@NotNull @PositiveOrZero BigDecimal amount, Currency currency) {
        this(amount, currency, DEFAULT_ROUNDING);
    }
    /**
     * Class constructor specifying amount. Uses default RoundingMode HALF_EVEN and default currency EUR.
     **/
    public Money(@NotNull @PositiveOrZero BigDecimal amount) {
        this(amount, EUR, DEFAULT_ROUNDING);
    }

    public Money() {
    }

    public BigDecimal increaseAmount(Money money) {
        setAmount(this.amount.add(money.amount));
        return this.amount;
    }

    public BigDecimal increaseAmount(BigDecimal addAmount) {
        setAmount(this.amount.add(addAmount));
        return this.amount;
    }

    public BigDecimal decreaseAmount(Money money) {
        setAmount(this.amount.subtract(money.getAmount()));
        return this.amount;
    }

    public BigDecimal decreaseAmount(BigDecimal addAmount) {
        setAmount(this.amount.subtract(addAmount));
        return this.amount;
    }

    public BigDecimal increaseByRate(BigDecimal rate) {
        setAmount(this.amount.add(this.amount.multiply(rate).setScale(2, Money.DEFAULT_ROUNDING)));
        return this.amount;
    }

    public BigDecimal decreaseByRate(BigDecimal rate) {
        setAmount(this.amount.subtract(this.amount.multiply(rate).setScale(2, Money.DEFAULT_ROUNDING)));
        return this.amount;
    }

    public Currency getCurrency() {
        return this.currency;
    }
    public BigDecimal getAmount() {
        return this.amount;
    }
    private void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String toString() {
        return getCurrency().getSymbol() + " " + getAmount();
    }
}
