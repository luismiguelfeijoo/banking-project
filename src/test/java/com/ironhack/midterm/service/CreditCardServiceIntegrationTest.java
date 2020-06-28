package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchCreditCardException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.CreditCard;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.AccountRepository;
import com.ironhack.midterm.repository.CreditCardRepository;
import com.ironhack.midterm.utils.Address;
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
class CreditCardServiceIntegrationTest {
    @Autowired
    private CreditCardService creditCardService;
    @Autowired
    private CreditCardRepository creditCardRepository;
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

    CreditCard creditCard;
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

        creditCard = new CreditCard(accountHolder);
        accountDTO = new AccountDTO(AccountType.CREDITCARD, new BigDecimal("0").setScale(2), accountHolder);
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
        accountHolderRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDCompleteAccountHolder_() {
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(accountDTO.getBalance(), newCreditCard.getBalance().getAmount());
        assertEquals(accountDTO.getPrimaryOwner().getName(), newCreditCard.getPrimaryOwner().getName());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwner() {
        accountDTO.setSecondaryOwner(accountHolder3);
        accountHolder.setId(null);
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(accountDTO.getBalance(), newCreditCard.getBalance().getAmount());
        assertEquals(accountHolder3.getName(), newCreditCard.getSecondaryOwner().getName());
    }

    @Test
    public void findById_ValidId_CreditCardFound() {
        creditCard.setPrimaryOwner(accountHolder2);
        creditCard = creditCardRepository.save(creditCard);
        CreditCard foundCreditCard = creditCardService.findById(creditCard.getId());

        assertEquals(creditCard.getBalance().getAmount(),foundCreditCard.getBalance().getAmount());
        assertEquals(creditCard.getInterestRate().setScale(2),foundCreditCard.getInterestRate());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchCreditCardException.class, ()-> creditCardService.findById((long) 10));
    }
}