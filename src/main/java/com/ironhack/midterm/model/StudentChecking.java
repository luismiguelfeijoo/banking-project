package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class StudentChecking extends Checking {
    
    public StudentChecking(Money balance, AccountHolder primaryOwner) {
        super(balance, primaryOwner);
        this.minimumBalance = new BigDecimal("0");
        this.monthlyMaintenanceFee = new BigDecimal("0");
    }

    public StudentChecking() {
        this.minimumBalance = new BigDecimal("0");
        this.monthlyMaintenanceFee = new BigDecimal("0");
    }
}
