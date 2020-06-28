package com.ironhack.midterm.controller.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class AmountDTO {
    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    public AmountDTO() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
