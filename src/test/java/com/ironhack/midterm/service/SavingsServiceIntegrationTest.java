package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchSavingsAccountException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Savings;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.AccountRepository;
import com.ironhack.midterm.repository.SavingsRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SavingsServiceIntegrationTest {
    @Autowired
    private SavingsService savingsService;
    @Autowired
    private SavingsRepository savingsRepository;
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

    Savings savings;
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

        savings = new Savings(new Money(new BigDecimal("2000")), accountHolder);
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
        Savings newSavings = savingsService.create(accountDTO);
        assertEquals(accountDTO.getBalance(), newSavings.getBalance().getAmount());
        assertEquals(accountDTO.getPrimaryOwner().getName(), newSavings.getPrimaryOwner().getName());
    }


    @Test
    public void findById_ValidId_SavingsFound() {
        savings.setPrimaryOwner(accountHolder2);
        savings = savingsRepository.save(savings);
        Savings foundSavings = savingsService.findById(savings.getId());
        assertEquals(savings.getSecretKey(),foundSavings.getSecretKey());
        assertEquals(savings.getMinimumBalance().setScale(2),foundSavings.getMinimumBalance());
        assertEquals(savings.getBalance().getAmount(),foundSavings.getBalance().getAmount());
        assertEquals(savings.getMonthlyMaintenanceFee().setScale(2),foundSavings.getMonthlyMaintenanceFee());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchSavingsAccountException.class, ()-> savingsService.findById((long) 10));
    }
}