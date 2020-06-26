package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchCreditCardException;
import com.ironhack.midterm.exceptions.NoSuchSavingsAccountException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.CreditCard;
import com.ironhack.midterm.model.Savings;
import com.ironhack.midterm.repository.CreditCardRepository;
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
class CreditCardServiceUnitTest {
    @Autowired
    private CreditCardService creditCardService;
    @MockBean
    private CreditCardRepository creditCardRepository;
    @MockBean
    private AccountHolderService accountHolderService;

    AccountDTO accountDTO;
    AccountHolder accountHolder;
    AccountHolder accountHolder2;
    CreditCard creditCard;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        calendar.set(1997, 12, 26);
        accountHolder2 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        creditCard = new CreditCard(accountHolder);
        accountDTO = new AccountDTO(AccountType.CHECKING, new BigDecimal("1000"), accountHolder);
        when(creditCardRepository.findById((long) 1)).thenReturn(Optional.of(creditCard));
        when(creditCardRepository.save(Mockito.any(CreditCard.class))).thenReturn(creditCard);
        when(accountHolderService.findById((long) 2)).thenReturn(accountHolder2);
        when(accountHolderService.findById((long) 1)).thenReturn(accountHolder);
        when(accountHolderService.create(Mockito.any(AccountHolder.class))).thenReturn(accountHolder2);
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDCompleteAccountHolder_() {
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(creditCard.getBalance(), newCreditCard.getBalance());
        assertEquals(creditCard.getPrimaryOwner(), newCreditCard.getPrimaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_NoSecondaryOwnerANDOnlyIdOfAccountHolder_() {
        accountHolder = new AccountHolder();
        accountHolder.setId((long) 1);
        accountDTO.setPrimaryOwner(accountHolder);
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(creditCard.getBalance(), newCreditCard.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwner() {
        accountDTO.setSecondaryOwner(accountHolder2);
        creditCard.setSecondaryOwner(accountHolder2);
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(creditCard.getBalance(), newCreditCard.getBalance());
        assertEquals(accountHolder2, newCreditCard.getSecondaryOwner());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_WithSecondaryOwnerOnlyId() {
        accountHolder2 = new AccountHolder();
        accountHolder2.setId((long) 2);
        accountDTO.setSecondaryOwner(accountHolder2);
        creditCard.setSecondaryOwner(accountHolder2);
        CreditCard newCreditCard = creditCardService.create(accountDTO);
        assertEquals(creditCard.getBalance(), newCreditCard.getBalance());
        assertEquals(accountHolder2, newCreditCard.getSecondaryOwner());
    }

    @Test
    public void findById_ValidId_CheckingFound() {
        CreditCard foundCreditCard = creditCardService.findById((long) 1);
        assertEquals(foundCreditCard.getCreditLimit(),foundCreditCard.getCreditLimit());
        assertEquals(foundCreditCard.getBalance(),foundCreditCard.getBalance());
        assertEquals(foundCreditCard.getInterestRate(),foundCreditCard.getInterestRate());
    }

    @Test
    public void findById_NotValidId_Exception() {
        assertThrows(NoSuchCreditCardException.class, ()-> creditCardService.findById((long) 2));
    }
}