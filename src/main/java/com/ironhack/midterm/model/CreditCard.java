package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import java.math.BigDecimal;

@Entity
public class CreditCard extends Account {
    // Maybe convert to Money ¿how to manage constraints?
    @DecimalMax(value = "100000")
    private BigDecimal creditLimit = new BigDecimal("100");
    @DecimalMin(value = "0.1")
    private BigDecimal interestRate = new BigDecimal("0.2");


    public CreditCard(AccountHolder primaryOwner, @DecimalMax(value = "100000") BigDecimal creditLimit, @DecimalMin(value = "0.1") BigDecimal interestRate) {
        super(new Money(new BigDecimal("0")), primaryOwner);
        this.creditLimit = creditLimit;
        this.interestRate = interestRate;
    }


    public CreditCard(AccountHolder primaryOwner) {
        super(new Money(new BigDecimal("0")), primaryOwner);
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public void creditAccount(Money amount) {
        if (this.getCreditLimit().compareTo(amount.getAmount().add(getBalance().getAmount())) >= 0) {
            BigDecimal newAmount = this.getBalance().increaseAmount(amount);
            setBalance(new Money(newAmount));
        } else {
            throw new NoEnoughBalanceException("There's not enough funds to do this transaction");
        }
    }

    @Override
    public void debitAccount(Money amount) {
        // check if balance is negative ?
        BigDecimal newAmount = this.getBalance().decreaseAmount(amount);
        setBalance(new Money(newAmount));
    }
}
