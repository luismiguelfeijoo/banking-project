package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class SavingsTest {
    AccountHolder accountHolder;
    Savings savings;
    Validator validator;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test", "test", "testPassword", calendar.getTime(), address);
        savings = new Savings(new Money(new BigDecimal("1000")),accountHolder);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void ApplyInterestRate_AYearPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Money previousBalance = savings.getBalance();
        savings.setLastInterestApplyDate(calendar.getTime());
        savings.applyInterestRate();
        assertEquals(previousBalance.increaseByRate(savings.getInterestRate()), savings.getBalance().getAmount());
    }

    @Test
    public void ApplyInterestRate_TWOMonthPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        savings.setLastInterestApplyDate(calendar.getTime());
        savings.applyInterestRate();
        assertEquals(new BigDecimal("1005.01"), savings.getBalance().getAmount());

    }

    @Test
    public void ApplyInterestRate_TWOANDAHALFMonthPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        calendar.add(Calendar.MONTH, -7);
        savings.setLastInterestApplyDate(calendar.getTime());
        savings.applyInterestRate();
        assertEquals(new BigDecimal("1005.01"), savings.getBalance().getAmount());
    }


}