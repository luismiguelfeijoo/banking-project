package com.ironhack.midterm.view_model;

import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.utils.Money;

import javax.persistence.Embedded;

public class AccountBalance {
    @Embedded
    private Money balance;

    public AccountBalance(Money balance) {
        this.balance = balance;
    }

    public Money getBalance() {
        return balance;
    }
}
