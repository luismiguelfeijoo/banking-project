package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Money;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Account debitedAccount;
    @ManyToOne
    private Account creditedAccount;
    @NotNull
    private Money amount;
    @NotNull
    private Date date;
    @NotNull
    @ManyToOne
    private User transactionMaker;

    public Transaction(@NotNull Money amount, @NotNull Date date, @NotNull User transactionMaker) {
        setAmount(amount);
        setDate(date);
        setTransactionMaker(transactionMaker);
    }

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getDebitedAccount() {
        return debitedAccount;
    }

    public void setDebitedAccount(Account debitedAccount) {
        this.debitedAccount = debitedAccount;
    }

    public Account getCreditedAccount() {
        return creditedAccount;
    }

    public void setCreditedAccount(Account creditedAccount) {
        this.creditedAccount = creditedAccount;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getTransactionMaker() {
        return transactionMaker;
    }

    public void setTransactionMaker(User transactionMaker) {
        this.transactionMaker = transactionMaker;
    }
}


