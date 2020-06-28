package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Hashing;
import com.ironhack.midterm.utils.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Checking extends Account {
    @Column(unique = true)
    @Type(type = "uuid-char")
    private UUID secretKey;

    private boolean penaltyCharged = false;
    protected BigDecimal minimumBalance = new BigDecimal("250");
    protected BigDecimal monthlyMaintenanceFee = new BigDecimal("12");
    private Date lastMonthlyMaintenanceFeeApplicationDate;

    private final static Logger LOGGER = LogManager.getLogger(Checking.class);

    /*
    public Checking(Money balance, User primaryOwner, User secondaryOwner) {
        super(balance, primaryOwner, secondaryOwner);
    }
     */

    public Checking(@Valid Money balance, @NotNull AccountHolder primaryOwner) {
        super(balance, primaryOwner);
        Date creationTime = new Date();
        // v4 UUID, the most secured of the easiest ways to create a random UUID
        this.secretKey = UUID.randomUUID();
        this.lastMonthlyMaintenanceFeeApplicationDate = new Date();
    }

    public Checking() {
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public BigDecimal getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    @Override
    public void creditAccount(@PositiveOrZero Money amount) {
        LOGGER.info("[CREDIT ACCOUNT INIT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        LOGGER.info("[CREDITING ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
        getBalance().increaseAmount(amount);
        LOGGER.info("[CREDITED ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount());
        if (penaltyCharged && getBalance().getAmount().compareTo(getMinimumBalance()) >= 0) penaltyCharged = false;
    }

    @Override
    public void debitAccount(@PositiveOrZero Money amount) {
        LOGGER.info("[DEBIT ACCOUNT INIT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount() + " - AccountType:" + this.getClass());
        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) throw new NegativeAmountException("The amount must be positive");
        if (this.getBalance().getAmount().compareTo(amount.getAmount()) >= 0) {
            LOGGER.info("[DEBITING ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount() + " - AccountType:" + this.getClass());
            getBalance().decreaseAmount(amount);
            LOGGER.info("[DEBITED ACCOUNT] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountDebited:" + amount.getAmount() + " - AccountType:" + this.getClass());
        } else {
            LOGGER.info("[DEBIT ACCOUNT REJECTED] Message: Not enough balance - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AmountToDebit:" + amount.getAmount()+ " - AccountType:" + this.getClass());
            throw new NoEnoughBalanceException("There's not enough funds to do this transaction");
        }
    }

    public void applyPenaltyFee() {
        if (!penaltyCharged && getBalance().getAmount().compareTo(getMinimumBalance()) < 0) {
            LOGGER.info("[APPLYING PENALTY FEE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AccountType:" + this.getClass());
            getBalance().decreaseAmount(getPenaltyFee());
            penaltyCharged = true;
        }
    }

    public void applyMaintenanceFee() {
        LOGGER.info("[CHECKING FOR MAINTENANCE FEE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AccountType:" + this.getClass());
        if (getMonthlyMaintenanceFee().compareTo(BigDecimal.ZERO) > 0) {
            int months = DateDifference.monthDifference(getLastMonthlyMaintenanceFeeApplicationDate());
            while (months >= 1) {
                LOGGER.info("[APPLYING MAINTENANCE FEE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + " - AccountType:" + this.getClass());
                getBalance().decreaseAmount(this.getMonthlyMaintenanceFee());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(getLastMonthlyMaintenanceFeeApplicationDate());
                calendar.add(Calendar.MONTH, 1);
                setLastInterestApplyDate(calendar.getTime());
                months--;
            }
        }
    }

    public UUID getSecretKey() {
        return secretKey;
    }

    public Date getLastMonthlyMaintenanceFeeApplicationDate() {
        return lastMonthlyMaintenanceFeeApplicationDate;
    }

    public void setLastMonthlyMaintenanceFeeApplicationDate(Date lastMonthlyMaintenanceFeeApplicationDate) {
        this.lastMonthlyMaintenanceFeeApplicationDate = lastMonthlyMaintenanceFeeApplicationDate;
    }


    /*
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
     */
}
