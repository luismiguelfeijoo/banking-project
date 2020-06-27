package com.ironhack.midterm.view_model;

import com.ironhack.midterm.model.User;
import com.ironhack.midterm.utils.Money;

import java.math.BigDecimal;

public class TransactionComplete {
    private BigDecimal amount;
    private AccountBalance userAccount;
    private String transactionMakerName;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public AccountBalance getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(AccountBalance userAccount) {
        this.userAccount = userAccount;
    }

    public String getTransactionMakerName() {
        return transactionMakerName;
    }

    public void setTransactionMakerName(String transactionMakerName) {
        this.transactionMakerName = transactionMakerName;
    }
}
