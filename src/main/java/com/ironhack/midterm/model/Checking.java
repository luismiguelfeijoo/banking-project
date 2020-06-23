package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Checking extends Account {
    private String secretKey;
    protected BigDecimal minimumBalance = new BigDecimal("250");
    protected BigDecimal monthlyMaintenanceFee = new BigDecimal("12");

    /*
    public Checking(Money balance, User primaryOwner, User secondaryOwner) {
        super(balance, primaryOwner, secondaryOwner);
    }
     */

    public Checking(Money balance, AccountHolder primaryOwner) {
        super(balance, primaryOwner);
    }

    public Checking() {
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public BigDecimal getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    /*
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

     */
}
