package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchSavingsAccountException;
import com.ironhack.midterm.exceptions.NoSuchStudentCheckingAccountException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Savings;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.SavingsRepository;
import com.ironhack.midterm.repository.StudentCheckingRepository;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class StudentCheckingServiceUnitTest {
    @Autowired
    private StudentCheckingService studentCheckingService;
    @MockBean
    private StudentCheckingRepository studentCheckingRepository;
    @MockBean
    private AccountHolderService accountHolderService;

    AccountDTO accountDTO;
    AccountHolder accountHolder;
    AccountHolder accountHolder2;
    StudentChecking studentChecking;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        calendar.set(1997, 12, 26);
        accountHolder2 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        studentChecking = new StudentChecking(new Money(new BigDecimal("2000")), accountHolder);
        accountDTO = new AccountDTO(AccountType.CHECKING, new BigDecimal("1000"), accountHolder);
        when(studentCheckingRepository.findById((long) 1)).thenReturn(Optional.of(studentChecking));
        when(studentCheckingRepository.save(Mockito.any(StudentChecking.class))).thenReturn(studentChecking);
        when(accountHolderService.findById((long) 2)).thenReturn(accountHolder2);
        when(accountHolderService.findById((long) 1)).thenReturn(accountHolder);
        when(accountHolderService.create(Mockito.any(AccountHolder.class))).thenReturn(accountHolder2);
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDCompleteAccountHolder_CreateAccount() {
        StudentChecking newStudentChecking = studentCheckingService.create(accountDTO);
        assertEquals(studentChecking.getBalance(), newStudentChecking.getBalance());
        assertEquals(studentChecking.getPrimaryOwner(), newStudentChecking.getPrimaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDOnlyIdOfAccountHolder_CreateAccount() {
        accountHolder = new AccountHolder();
        accountHolder.setId((long) 1);
        accountDTO.setPrimaryOwner(accountHolder);
        StudentChecking newStudentChecking = studentCheckingService.create(accountDTO);
        assertEquals(studentChecking.getBalance(), newStudentChecking.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwner() {
        accountDTO.setSecondaryOwner(accountHolder2);
        studentChecking.setSecondaryOwner(accountHolder2);
        StudentChecking newStudentChecking = studentCheckingService.create(accountDTO);
        assertEquals(studentChecking.getBalance(), newStudentChecking.getBalance());
        assertEquals(accountHolder2, newStudentChecking.getSecondaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwnerOnlyId() {
        accountHolder2 = new AccountHolder();
        accountHolder2.setId((long) 2);
        accountDTO.setSecondaryOwner(accountHolder2);
        studentChecking.setSecondaryOwner(accountHolder2);
        StudentChecking newStudentChecking = studentCheckingService.create(accountDTO);
        assertEquals(studentChecking.getBalance(), newStudentChecking.getBalance());
        assertEquals(accountHolder2, newStudentChecking.getSecondaryOwner());
    }

    @Test
    public void findById_ValidId_CheckingFound() {
        StudentChecking foundStudentChecking = studentCheckingService.findById((long) 1);
        assertEquals(studentChecking.getSecretKey(),foundStudentChecking.getSecretKey());
        assertEquals(studentChecking.getMinimumBalance(),foundStudentChecking.getMinimumBalance());
        assertEquals(studentChecking.getBalance(),foundStudentChecking.getBalance());
        assertEquals(studentChecking.getMonthlyMaintenanceFee(),foundStudentChecking.getMonthlyMaintenanceFee());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchStudentCheckingAccountException.class, ()-> studentCheckingService.findById((long) 2));
    }
}