package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.PositiveOrZero;
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

    private final static Logger LOGGER = LogManager.getLogger(CreditCard.class);

    public CreditCard() {
    }

    public CreditCard(AccountHolder primaryOwner, @DecimalMax(value = "100000") @DecimalMin(value = "100") BigDecimal creditLimit, @DecimalMin(value = "0.1") @DecimalMax(value = "0.2") BigDecimal interestRate) {
        super(new Money(new BigDecimal("0")), primaryOwner);
        setCreditLimit(creditLimit);
        setInterestRate(interestRate);
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
    public void creditAccount(@PositiveOrZero Money amount) {
        // check if balance is negative ?
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        this.getBalance().decreaseAmount(amount);
    }

    @Override
    public void debitAccount(@PositiveOrZero Money amount) {
        LOGGER.info("[CREDIT ACCOUNT INIT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        if (this.getCreditLimit().compareTo(amount.getAmount().add(getBalance().getAmount())) >= 0) {
            LOGGER.info("[CREDITING ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
            getBalance().increaseAmount(amount);
            LOGGER.info("[CREDITING ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountDebited:" + amount.getAmount());
        } else {
            LOGGER.info("[CREDIT ACCOUNT REJECTED] Message: Not enough credit limit - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
            throw new NoEnoughBalanceException("There's not enough credit limit to do the transaction");
        }
    }

    public void applyInterestRate() {
        LOGGER.info("[CHECKING FOR INTEREST RATE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + "lastDateOfCharge:" + getLastInterestApplyDate() + "interestRate:" + getInterestRate());
        int months = DateDifference.monthDifference(getLastInterestApplyDate());
        while (months >= 1) {
            LOGGER.info("[APPLYING INTEREST RATE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + "lastDateOfCharge:" + getLastInterestApplyDate() + "interestRate:" + getInterestRate());
            getBalance().increaseByRate(BigDecimal.ONE.multiply(getInterestRate().divide(new BigDecimal("12"), 4, RoundingMode.HALF_EVEN)));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLastInterestApplyDate());
            calendar.add(Calendar.MONTH, 1);
            setLastInterestApplyDate(calendar.getTime());
            months--;
        }
    }
}
