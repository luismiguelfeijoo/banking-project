package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Entity
public class Savings extends Checking {
    @DecimalMax(value = "0.5")
    private BigDecimal interestRate = new BigDecimal("0.0025");



    public Savings() {
    }

    /*
    public Savings(Money balance, User primaryOwner, User secondaryOwner, BigDecimal interestRate) {
        super(balance, primaryOwner, secondaryOwner);
        setInterestRate(interestRate);
    }
     */

    public Savings(Money balance, AccountHolder primaryOwner, @DecimalMax(value = "0.5") BigDecimal interestRate) {
        super(balance, primaryOwner);
        this.interestRate = interestRate;
    }

    /*
    public Savings(Money balance, User primaryOwner, User secondaryOwner) {
        super(balance, primaryOwner, secondaryOwner);
    }
     */

    public Savings(Money balance, AccountHolder primaryOwner) {
        super(balance, primaryOwner);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }


    /*
    private void setInterestRate(BigDecimal interestRate)  {
        this.interestRate = interestRate;
    }
     */
}
