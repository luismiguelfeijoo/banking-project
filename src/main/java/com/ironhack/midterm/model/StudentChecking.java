package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Money;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class StudentChecking extends Account{
    public StudentChecking() {
    }

    /*
    public StudentChecking(Money balance, User primaryOwner, User secondaryOwner) {
        super(balance, primaryOwner, secondaryOwner);
    }
     */

    public StudentChecking(Money balance, AccountHolder primaryOwner) {
        super(balance, primaryOwner);
    }
}
