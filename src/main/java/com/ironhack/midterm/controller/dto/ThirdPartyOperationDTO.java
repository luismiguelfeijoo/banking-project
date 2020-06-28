package com.ironhack.midterm.controller.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public class ThirdPartyOperationDTO {
    @NotNull
    @PositiveOrZero
    private BigDecimal amount;
    private UUID accountSecretKey;

    public ThirdPartyOperationDTO() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UUID getAccountSecretKey() {
        return accountSecretKey;
    }

    public void setAccountSecretKey(UUID accountSecretKey) {
        this.accountSecretKey = accountSecretKey;
    }
}
