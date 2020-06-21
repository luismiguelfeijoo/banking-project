package com.ironhack.midterm.model;

import com.ironhack.midterm.enums.AccountStatus;
import com.ironhack.midterm.utils.Money;
import nl.garvelink.iban.IBAN;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Optional;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amount", column=@Column(name="account_balance")),
            @AttributeOverride(name="currency", column=@Column(name="account_currency"))
    })
    private Money balance;

    @ManyToOne
    private User primaryOwner;
    @ManyToOne
    private User secondaryOwner;
    private BigDecimal penaltyFee;
    private AccountStatus status;

    public Account() {
    }

    /**
     * Class constructor specifying balance, both owners
     **/
    public Account(Money balance, User primaryOwner, User secondaryOwner, BigDecimal penaltyFee) {
        this.balance = balance;
        this.primaryOwner = primaryOwner;
        this.secondaryOwner = secondaryOwner;
        this.penaltyFee = penaltyFee;
        this.status = AccountStatus.ACTIVE;
    }

    /**
     * Class constructor specifying balance, both owners
     **/
    public Account(Money balance, User primaryOwner, BigDecimal penaltyFee) {
        this.balance = balance;
        this.primaryOwner = primaryOwner;
        this.secondaryOwner = null;
        this.penaltyFee = penaltyFee;
        this.status = AccountStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Money getBalance() {
        return balance;
    }

    public void setBalance(Money balance) {
        this.balance = balance;
    }

    public User getPrimaryOwner() {
        return primaryOwner;
    }

    public void setPrimaryOwner(User primaryOwner) {
        this.primaryOwner = primaryOwner;
    }

    public User getSecondaryOwner() {
        return secondaryOwner;
    }

    public void setSecondaryOwner(User secondaryOwner) {
        this.secondaryOwner = secondaryOwner;
    }

    public BigDecimal getPenaltyFee() {
        return penaltyFee;
    }

    public void setPenaltyFee(BigDecimal penaltyFee) {
        this.penaltyFee = penaltyFee;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
