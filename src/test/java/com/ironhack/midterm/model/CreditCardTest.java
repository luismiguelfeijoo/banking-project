package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardTest {
    CreditCard creditCard;
    AccountHolder accountHolder;
    Validator validator;

    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test", "test", "testPassword", calendar.getTime(), address);
        creditCard = new CreditCard(accountHolder);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }



    @Test
    public void changeCreditLimit_ValidLimit_LimitChanged() {
        //CreditCard creditCard = new CreditCard(accountHolder, new BigDecimal("2500"), new BigDecimal("0.1"));
        creditCard.setCreditLimit(new BigDecimal("2500"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertTrue(violations.isEmpty());
        assertEquals(new BigDecimal("2500"), creditCard.getCreditLimit());
    }

    @Test
    public void changeCreditLimit_SmallerThanValid_LimitChanged() {
        creditCard.setCreditLimit(new BigDecimal("90"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertFalse(violations.isEmpty());
        //assertEquals(new BigDecimal("2500"), creditCard.getCreditLimit());
    }

    @Test
    public void changeCreditLimit_BiggerThanValid_LimitChanged() {
        creditCard.setCreditLimit(new BigDecimal("100000000"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertFalse(violations.isEmpty());
        //assertEquals(new BigDecimal("2500"), creditCard.getCreditLimit());
    }

    @Test
    public void changeInterestRate_ValidRate_LimitChanged() {
        //CreditCard creditCard = new CreditCard(accountHolder, new BigDecimal("2500"), new BigDecimal("0.1"));
        creditCard.setInterestRate(new BigDecimal("0.15"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertTrue(violations.isEmpty());
        assertEquals(new BigDecimal("0.15"), creditCard.getInterestRate());
    }

    @Test
    public void changeInterestRate_SmallerThanValid_LimitChanged() {
        creditCard.setCreditLimit(new BigDecimal("0.09"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertFalse(violations.isEmpty());
        //assertEquals(new BigDecimal("2500"), creditCard.getCreditLimit());
    }

    @Test
    public void changeInterestRate_BiggerThanValid_LimitChanged() {
        creditCard.setCreditLimit(new BigDecimal("0.21"));
        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        assertFalse(violations.isEmpty());
        //assertEquals(new BigDecimal("2500"), creditCard.getCreditLimit());
    }

    @Test
    public void debitAccount_EnoughLimit_AccountDebited() {
        BigDecimal previousBalance = creditCard.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("100"));
        creditCard.debitAccount(amount);
        assertEquals(previousBalance.add(amount.getAmount()), creditCard.getBalance().getAmount());
    }

    @Test
    public void debitAccount_NotEnoughLimit_ThrowsException() {
        BigDecimal creditLimit = creditCard.getCreditLimit();
        Money amount = new Money(creditLimit.add(new BigDecimal("1000")));
        assertThrows(NoEnoughBalanceException.class, () -> creditCard.debitAccount(amount));
    }

    @Test
    public void debitAccount_NegativeAmount_ThrowError() {
        Money amount = new Money(new BigDecimal("-100"));
        assertThrows(NegativeAmountException.class, () -> creditCard.debitAccount(amount));
    }

    @Test
    public void creditAccount_PositiveAmount_AccountDebited() {
        BigDecimal previousBalance = creditCard.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("100"));
        creditCard.creditAccount(amount);
        assertEquals(previousBalance.subtract(amount.getAmount()), creditCard.getBalance().getAmount());
    }

    @Test
    public void creditAccount_NegativeAmount_ThrowError() {
        Money amount = new Money(new BigDecimal("-100"));
        assertThrows(NegativeAmountException.class, () -> creditCard.creditAccount(amount));
    }

    @Test
    public void ApplyInterestRate_NotAMonth_NothingChanges() {
        BigDecimal previousBalance = creditCard.getBalance().getAmount();
        creditCard.applyInterestRate();
        assertEquals(previousBalance, creditCard.getBalance().getAmount());
    }

    @Test
    public void ApplyInterestRate_AMonthPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        creditCard.setBalance(new Money(new BigDecimal("500")));
        Money previousBalance = creditCard.getBalance();
        creditCard.setLastInterestApplyDate(calendar.getTime());
        creditCard.applyInterestRate();
        assertEquals(previousBalance.increaseByRate(creditCard.getInterestRate().divide(new BigDecimal("12"), 4, RoundingMode.HALF_EVEN)), creditCard.getBalance().getAmount());
    }

    @Test
    public void ApplyInterestRate_TWOMonthPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        creditCard.setBalance(new Money(new BigDecimal("500")));
        Money previousBalance = creditCard.getBalance();
        creditCard.setLastInterestApplyDate(calendar.getTime());
        creditCard.applyInterestRate();
        assertEquals(new BigDecimal("516.84"), creditCard.getBalance().getAmount());
    }

    @Test
    public void ApplyInterestRate_TWOANDAHALFMonthPassed_NothingChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        creditCard.setBalance(new Money(new BigDecimal("500")));
        Money previousBalance = creditCard.getBalance();
        creditCard.setLastInterestApplyDate(calendar.getTime());
        creditCard.applyInterestRate();
        assertEquals(new BigDecimal("516.84"), creditCard.getBalance().getAmount());
    }



}