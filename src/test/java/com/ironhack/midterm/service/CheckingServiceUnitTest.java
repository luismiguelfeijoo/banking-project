package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchCheckingAccountException;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.CheckingRepository;
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
class CheckingServiceUnitTest {
    @Autowired
    private CheckingService checkingService;
    @MockBean
    private CheckingRepository checkingRepository;
    @MockBean
    private AccountHolderService accountHolderService;
    @MockBean
    private StudentCheckingService studentCheckingService;

    AccountDTO accountDTO;
    AccountHolder accountHolder;
    AccountHolder accountHolder2;

    Checking checking;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        calendar.set(1997, 12, 26);
        accountHolder2 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        checking = new Checking(new Money(new BigDecimal("2000")), accountHolder);
        accountDTO = new AccountDTO(AccountType.CHECKING, new BigDecimal("1000"), accountHolder);
        when(checkingRepository.findById((long) 1)).thenReturn(Optional.of(checking));
        when(checkingRepository.save(Mockito.any(Checking.class))).thenReturn(checking);
        when(accountHolderService.findById((long) 2)).thenReturn(accountHolder2);
        when(accountHolderService.findById((long) 1)).thenReturn(accountHolder);
        when(accountHolderService.create(Mockito.any(AccountHolder.class))).thenReturn(accountHolder2);
        when(studentCheckingService.create(Mockito.any(AccountDTO.class))).thenReturn(new StudentChecking());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDCompleteAccountHolder_() {
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(checking.getBalance(), newChecking.getBalance());
        assertEquals(checking.getPrimaryOwner(), newChecking.getPrimaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDOnlyIdOfAccountHolder_() {
        accountHolder = new AccountHolder();
        accountHolder.setId((long) 1);
        accountDTO.setPrimaryOwner(accountHolder);
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(checking.getBalance(), newChecking.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwner() {
        accountDTO.setSecondaryOwner(accountHolder2);
        checking.setSecondaryOwner(accountHolder2);
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(checking.getBalance(), newChecking.getBalance());
        assertEquals(accountHolder2, newChecking.getSecondaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwnerOnlyId() {
        accountHolder2 = new AccountHolder();
        accountHolder2.setId((long) 2);
        accountDTO.setSecondaryOwner(accountHolder2);
        checking.setSecondaryOwner(accountHolder2);
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(checking.getBalance(), newChecking.getBalance());
        assertEquals(accountHolder2, newChecking.getSecondaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithYoungUser_ReturnAStudentChecking() {
        accountDTO.setPrimaryOwner(accountHolder2);
        Account newChecking = studentCheckingService.create(accountDTO);
        assertTrue(newChecking instanceof StudentChecking );
    }

    @Test
    public void findById_ValidId_CheckingFound() {
        Checking foundChecking = checkingService.findById((long) 1);
        assertEquals(checking.getSecretKey(),foundChecking.getSecretKey());
        assertEquals(checking.getMinimumBalance(),foundChecking.getMinimumBalance());
        assertEquals(checking.getBalance(),foundChecking.getBalance());
        assertEquals(checking.getMonthlyMaintenanceFee(),foundChecking.getMonthlyMaintenanceFee());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchCheckingAccountException.class, ()-> checkingService.findById((long) 2));
    }
}