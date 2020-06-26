package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.Hashing;
import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Checking extends Account {
    @NotNull
    private String secretKey;
    private boolean penaltyCharged = false;
    protected BigDecimal minimumBalance = new BigDecimal("250");
    protected BigDecimal monthlyMaintenanceFee = new BigDecimal("12");


    /*
    public Checking(Money balance, User primaryOwner, User secondaryOwner) {
        super(balance, primaryOwner, secondaryOwner);
    }
     */

    public Checking(@Valid Money balance, @NotNull AccountHolder primaryOwner) {
        super(balance, primaryOwner);
        Date creationTime = new Date();
        this.secretKey = Hashing.hash(creationTime.toString());
    }

    public Checking() {
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public BigDecimal getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    @Override
    public void creditAccount(@PositiveOrZero Money amount) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        if (this.getBalance().getAmount().compareTo(amount.getAmount()) >= 0) {
            getBalance().decreaseAmount(amount);
        } else {
            throw new NoEnoughBalanceException("There's not enough funds to do this transaction");
        }
    }

    @Override
    public void debitAccount(@PositiveOrZero Money amount) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        getBalance().increaseAmount(amount);
        if (penaltyCharged && getBalance().getAmount().compareTo(getMinimumBalance()) >= 0) penaltyCharged = false;
    }

    public void applyPenaltyFee() {
        if (!penaltyCharged && getBalance().getAmount().compareTo(getMinimumBalance()) < 0) {
            getBalance().decreaseAmount(getPenaltyFee());
            penaltyCharged = true;
        }
    }


    public String getSecretKey() {
        return secretKey;
    }

    /*
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
     */
}
