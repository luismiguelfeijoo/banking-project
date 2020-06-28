package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Savings extends Checking {
    @DecimalMax(value = "0.5")
    private BigDecimal interestRate = new BigDecimal("0.0025");

    private final static Logger LOGGER = LogManager.getLogger(Savings.class);

    public Savings() {
    }

    /*
    public Savings(Money balance, User primaryOwner, User secondaryOwner, BigDecimal interestRate) {
        super(balance, primaryOwner, secondaryOwner);
        setInterestRate(interestRate);
    }
     */

    /*
    public Savings(Money balance, AccountHolder primaryOwner, @DecimalMax(value = "0.5") BigDecimal interestRate) {
        super(balance, primaryOwner);
        this.interestRate = interestRate;
    }
     */

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

    public void setInterestRate(BigDecimal interestRate)  {
        this.interestRate = interestRate;
    }

    public void applyInterestRate() {
        LOGGER.info("[CHECKING FOR INTEREST RATE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + "lastDateOfCharge:" + getLastInterestApplyDate() + "interestRate:" + getInterestRate());
        int years = DateDifference.yearDifference(getLastInterestApplyDate());
        while (years >= 1) {
            LOGGER.info("[APPLYING INTEREST RATE] - AccountId:" + getId() + " - AccountBalance:" + getBalance() + "lastDateOfCharge:" + getLastInterestApplyDate() + "interestRate:" + getInterestRate());
            getBalance().increaseByRate(BigDecimal.ONE.multiply(getInterestRate()).setScale(4, RoundingMode.HALF_EVEN));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLastInterestApplyDate());
            calendar.add(Calendar.YEAR, 1);
            setLastInterestApplyDate(calendar.getTime());
            years--;
        }
    }

}
