package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.DuplicatedUsernameException;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.RoleRepository;
import com.ironhack.midterm.utils.Address;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AccountHolderServiceIntegrationTest {
    @Autowired
    private AccountHolderService accountHolderService;
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private RoleRepository roleRepository;

    Address address;
    AccountHolder accountHolder1;
    AccountHolder accountHolder2;
    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 12, 26);
        address = new Address("test street", "test city", "test country", "00000");
        accountHolder1 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        calendar.set(2000, 01, 10);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(),address);
        accountHolder2.setMailingAddress(address);
        calendar.set(1990, 03, 17);
        AccountHolder accountHolder3 = new AccountHolder("test3", "test3", "testPassword", calendar.getTime(),address);
        accountHolder3 = accountHolderRepository.save(accountHolder3);
    }

    @AfterEach
    public void tearDown() {
        accountHolderRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_ValidAccountHolderNoMailingAddress_AuthAdmin() {
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder1);
        assertEquals(accountHolder1.getDateOfBirth(), newAccountHolder.getDateOfBirth());
        assertEquals(accountHolder1.getPrimaryAddress(), newAccountHolder.getPrimaryAddress());
        assertEquals(null, newAccountHolder.getMailingAddress());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_ValidAccountHolderWithMailingAddress_AuthAdmin() {
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder2);
        assertEquals(accountHolder2.getDateOfBirth(), newAccountHolder.getDateOfBirth());
        assertEquals(accountHolder2.getPrimaryAddress(), newAccountHolder.getPrimaryAddress());
        assertEquals(accountHolder2.getMailingAddress(), newAccountHolder.getMailingAddress());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_RepeatedUsernameAccountHolder_ThrowException() {
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder1);
        assertThrows(DuplicatedUsernameException.class, () -> accountHolderService.create(accountHolder1));
    }

    @Test
    public void Create_ValidAccountHolder_NoAuth() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()-> accountHolderService.create(accountHolder1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void findById_ValidId_AccountHolder() {
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder1);
        AccountHolder result = accountHolderService.findById(newAccountHolder.getId());
        assertEquals("test1", result.getName());
        assertEquals("test1", result.getUsername());
    }

    @Test
    public void findById_NOTValidId_AccountHolder() {
        assertThrows(NoSuchAccountHolderException.class, () -> accountHolderService.findById((long) 4));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void findAll() {
        List<AccountHolder> accountHolderList = accountHolderService.findAll();
        assertEquals(1, accountHolderList.size());
    }
}