package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchCheckingAccountException;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.AccountRepository;
import com.ironhack.midterm.repository.CheckingRepository;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckingServiceIntegrationTest {
    @Autowired
    private CheckingService checkingService;
    @Autowired
    private CheckingRepository checkingRepository;
    @Autowired
    private AccountHolderService accountHolderService;
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private StudentCheckingService studentCheckingService;

    AccountDTO accountDTO;
    AccountHolder accountHolder;
    AccountHolder accountHolder2;
    AccountHolder accountHolder3;

    Checking checking;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        calendar.set(1997, 12, 26);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(), address);
        accountHolder2 = accountHolderRepository.save(accountHolder2);
        accountHolder3 = new AccountHolder("test3", "test3", "testPassword", calendar.getTime(), address);

        checking = new Checking(new Money(new BigDecimal("2000")), accountHolder);
        accountDTO = new AccountDTO(AccountType.CHECKING, new BigDecimal("1000").setScale(2), accountHolder);
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
        accountHolderRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDCompleteAccountHolder_() {
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(accountDTO.getBalance(), newChecking.getBalance().getAmount());
        assertEquals(accountDTO.getPrimaryOwner().getName(), newChecking.getPrimaryOwner().getName());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwner() {
        accountDTO.setSecondaryOwner(accountHolder3);
        accountHolder.setId(null);
        Checking newChecking = checkingService.create(accountDTO);
        assertEquals(accountDTO.getBalance(), newChecking.getBalance().getAmount());
        assertEquals(accountHolder3.getName(), newChecking.getSecondaryOwner().getName());
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
        checking.setPrimaryOwner(accountHolder2);
        checking = checkingRepository.save(checking);
        Checking foundChecking = checkingService.findById(checking.getId());
        assertEquals(checking.getSecretKey(),foundChecking.getSecretKey());
        assertEquals(checking.getMinimumBalance().setScale(2),foundChecking.getMinimumBalance());
        assertEquals(checking.getBalance().getAmount(),foundChecking.getBalance().getAmount());
        assertEquals(checking.getMonthlyMaintenanceFee().setScale(2),foundChecking.getMonthlyMaintenanceFee());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchCheckingAccountException.class, ()-> checkingService.findById((long) 10));
    }
}