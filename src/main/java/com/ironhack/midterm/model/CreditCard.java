package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Entity
public class CreditCard extends Account {
    // Maybe convert to Money ¿how to manage constraints?
    @DecimalMax(value = "100000")
    @DecimalMin(value = "100")
    private BigDecimal creditLimit = new BigDecimal("100");
    @DecimalMin(value = "0.1")
    @DecimalMax(value = "0.2")
    private BigDecimal interestRate = new BigDecimal("0.2");


    public CreditCard(AccountHolder primaryOwner,  @DecimalMax(value = "100000") @DecimalMin(value = "100") BigDecimal creditLimit, @DecimalMin(value = "0.1") @DecimalMax(value = "0.2") BigDecimal interestRate) {
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

    public void setCreditLimit( @DecimalMax(value = "100000") @DecimalMin(value = "100") BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(@DecimalMin(value = "0.1") @DecimalMax(value = "0.2") BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public void creditAccount(Money amount) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        if (this.getCreditLimit().compareTo(amount.getAmount().add(getBalance().getAmount())) >= 0) {
            getBalance().increaseAmount(amount);
        } else {
            throw new NoEnoughBalanceException("There's not enough credit limit to do the transaction");
        }
    }

    @Override
    public void debitAccount(Money amount) {
        // check if balance is negative ?
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        this.getBalance().decreaseAmount(amount);
    }

    public void applyInterestRate() {
        int months = DateDifference.monthDifference(getLastInterestApplyDate());
        System.out.println(months);
        while (months >= 1) {
            getBalance().increaseByRate(BigDecimal.ONE.multiply(getInterestRate().divide(new BigDecimal("12"), 4, RoundingMode.HALF_EVEN)));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLastInterestApplyDate());
            calendar.add(Calendar.MONTH, 1);
            setLastInterestApplyDate(calendar.getTime());
            months--;
            System.out.println(getBalance());
            System.out.println(getLastInterestApplyDate());
        }
    }
}
