package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class StudentCheckingTest {
    @Test
    public void createStudentChecking_0MinimumBalance() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        AccountHolder accountHolder = new AccountHolder("test", "test", "testPassword", calendar.getTime(), address);
        StudentChecking studentChecking = new StudentChecking(new Money(new BigDecimal("1000")), accountHolder);
        assertEquals(BigDecimal.ZERO, studentChecking.getMinimumBalance());
        assertEquals(BigDecimal.ZERO, studentChecking.getMonthlyMaintenanceFee());

    }

}