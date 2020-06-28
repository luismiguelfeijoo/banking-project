package com.ironhack.midterm.model;

import com.ironhack.midterm.enums.AccountStatus;
import com.ironhack.midterm.utils.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name="amount", column=@Column(name="account_balance")),
            @AttributeOverride(name="currency", column=@Column(name="account_currency"))
    })
    private Money balance;

    @NotNull
    @ManyToOne
    private AccountHolder primaryOwner;
    @ManyToOne
    private AccountHolder secondaryOwner;
    private final BigDecimal penaltyFee = new BigDecimal("40");

    @OneToMany(mappedBy = "debitedAccount")
    private List<Transaction> debitTransactions;

    @OneToMany(mappedBy = "creditedAccount")
    private List<Transaction> creditTransactions;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;
    private Date lastInterestApplyDate;

    private final static Logger LOGGER = LogManager.getLogger(Account.class);

    public Account() {
    }

    /**
     * Class constructor specifying balance, both owners
     **/
    /*
    public Account(Money balance, User primaryOwner, User secondaryOwner) {
        this.balance = balance;
        this.primaryOwner = primaryOwner;
        this.secondaryOwner = secondaryOwner;
        this.status = AccountStatus.ACTIVE;
    }
     */

    /**
     * Class constructor specifying balance, both owners
     **/
    public Account(Money balance, AccountHolder primaryOwner) {
        this.balance = balance;
        setPrimaryOwner(primaryOwner);
        setSecondaryOwner(null);
        setLastInterestApplyDate(new Date());
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

    public void setPrimaryOwner(AccountHolder primaryOwner) {
        this.primaryOwner = primaryOwner;
    }

    public User getSecondaryOwner() {
        return secondaryOwner;
    }

    public void setSecondaryOwner(AccountHolder secondaryOwner) {
        this.secondaryOwner = secondaryOwner;
    }

    public BigDecimal getPenaltyFee() {
        return penaltyFee;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Date getLastInterestApplyDate() {
        return lastInterestApplyDate;
    }

    public void setLastInterestApplyDate(Date lastInterestApplyDate) {
        this.lastInterestApplyDate = lastInterestApplyDate;
    }

    /*
    public List<Transaction> getDebitTransactions() {
        return debitTransactions;
    }

    public void setDebitTransactions(List<Transaction> debitTransactions) {
        this.debitTransactions = debitTransactions;
    }

    public List<Transaction> getCreditTransactions() {
        return creditTransactions;
    }

    public void setCreditTransactions(List<Transaction> creditTransactions) {
        this.creditTransactions = creditTransactions;
    }
     */

    public abstract void creditAccount(@PositiveOrZero Money amount);

    public abstract void debitAccount (@PositiveOrZero Money amount);

    public boolean hasAccess(Long userId) {
        LOGGER.info("[CHECKING ACCOUNT ACCESS] - AccountId:" + getId() + " - userId:" + userId + " - AccountType:" + this.getClass());
        if (userId.equals(this.primaryOwner.getId())) {
            return true;
        } else return this.secondaryOwner != null && userId.equals(this.secondaryOwner.getId());
    }

}
