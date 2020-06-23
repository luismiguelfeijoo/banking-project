package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Hashing;
import com.ironhack.midterm.utils.Money;

import javax.crypto.SecretKey;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Checking extends Account {
    @NotNull
    private String secretKey;
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

    /*
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

     */
}
