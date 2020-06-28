package com.ironhack.midterm.model;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


class CheckingTest {
    Checking checking;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        AccountHolder accountHolder = new AccountHolder("test", "test", "testPassword", calendar.getTime(), address);
        checking = new Checking(new Money(new BigDecimal("1000")), accountHolder);
    }

    @Test
    public void debitAccount_EnoughFunds_AccountDebited() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("100"));
        checking.debitAccount(amount);
        assertEquals(previousBalance.subtract(amount.getAmount()), checking.getBalance().getAmount());
    }

    @Test
    public void debitAccount_NotEnoughFunds_ThrowsException() {
        Money amount = new Money(new BigDecimal("10000"));
        assertThrows(NoEnoughBalanceException.class, () -> checking.debitAccount(amount));
    }

    @Test
    public void debitAccount_NegativeAmount_ThrowError() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("-100"));
        assertThrows(NegativeAmountException.class, () -> checking.debitAccount(amount));
    }

    @Test
    public void creditAccount_PositiveAmount_AccountCredited() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("100"));
        checking.creditAccount(amount);
        assertEquals(previousBalance.add(amount.getAmount()), checking.getBalance().getAmount());
    }

    @Test
    public void creditAccount_NegativeAmount_ThrowError() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Money amount = new Money(new BigDecimal("-100"));
        assertThrows(NegativeAmountException.class, () -> checking.creditAccount(amount));
    }

    @Test
    public void applyPenaltyFee_GreaterBalanceThanMinimum_NothingChanges() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        checking.applyPenaltyFee();
        assertEquals(previousBalance, checking.getBalance().getAmount());
    }

    @Test
    public void applyPenaltyFee_SmallerBalanceThanMinimum_BalanceDrops() {
        checking.setBalance(new Money(checking.getMinimumBalance().subtract(new BigDecimal("1"))));
        BigDecimal previousBalance = checking.getBalance().getAmount();
        checking.applyPenaltyFee();
        assertEquals(previousBalance, checking.getBalance().getAmount().add(checking.getPenaltyFee()));
    }

    @Test
    public void applyMaintenanceFee_NOTAMonthPassed_NotCharged() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        checking.applyMaintenanceFee();
        assertEquals(previousBalance, checking.getBalance().getAmount());
    }

    @Test
    public void applyMaintenanceFee_AMonthPassed_Charged() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        checking.setLastMonthlyMaintenanceFeeApplicationDate(calendar.getTime());
        checking.applyMaintenanceFee();
        assertEquals(previousBalance.subtract(checking.getMonthlyMaintenanceFee()), checking.getBalance().getAmount());
    }

    @Test
    public void applyMaintenanceFee_TWOMonthPassed_Charged() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        checking.setLastMonthlyMaintenanceFeeApplicationDate(calendar.getTime());
        checking.applyMaintenanceFee();
        assertEquals(previousBalance.subtract(checking.getMonthlyMaintenanceFee().multiply(new BigDecimal("2"))), checking.getBalance().getAmount());
    }

    @Test
    public void applyMaintenanceFee_TWOANDHALFMonthPassed_Charged() {
        BigDecimal previousBalance = checking.getBalance().getAmount();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        calendar.add(Calendar.DAY_OF_MONTH, -10);
        checking.setLastMonthlyMaintenanceFeeApplicationDate(calendar.getTime());
        checking.applyMaintenanceFee();
        assertEquals(previousBalance.subtract(checking.getMonthlyMaintenanceFee().multiply(new BigDecimal("2"))), checking.getBalance().getAmount());
    }

}