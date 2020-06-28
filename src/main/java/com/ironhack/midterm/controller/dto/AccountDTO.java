package com.ironhack.midterm.controller.dto;

import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.model.AccountHolder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class AccountDTO {
    @NotNull
    private AccountType accountType;
    @PositiveOrZero
    private BigDecimal balance;
    @NotNull
    private AccountHolder primaryOwner;

    private AccountHolder secondaryOwner;
    private BigDecimal creditLimit;
    private BigDecimal interestRate;

    public AccountDTO(@NotNull AccountType accountType, @NotNull @PositiveOrZero BigDecimal balance, @Valid @NotNull AccountHolder primaryOwner) {
        setAccountType(accountType);
        setBalance(balance);
        setPrimaryOwner(primaryOwner);
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountHolder getPrimaryOwner() {
        return primaryOwner;
    }

    public void setPrimaryOwner(AccountHolder primaryOwner) {
        this.primaryOwner = primaryOwner;
    }

    public AccountHolder getSecondaryOwner() {
        return secondaryOwner;
    }

    public void setSecondaryOwner(AccountHolder secondaryOwner) {
        this.secondaryOwner = secondaryOwner;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
}
