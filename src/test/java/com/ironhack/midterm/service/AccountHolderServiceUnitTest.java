package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.DuplicatedUsernameException;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.exceptions.UserAlreadyLoggedInException;
import com.ironhack.midterm.exceptions.UserNotLoggedInException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.RoleRepository;
import com.ironhack.midterm.utils.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class AccountHolderServiceUnitTest {
    @Autowired
    private AccountHolderService accountHolderService;
    @MockBean
    private AccountHolderRepository accountHolderRepository;
    @MockBean
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
        accountHolder1.setId((long) 1);
        calendar.set(2000, 01, 10);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(),address);
        accountHolder2.setId((long) 2);
        accountHolder2.setMailingAddress(address);
        calendar.set(1990, 03, 17);
        AccountHolder accountHolder3 = new AccountHolder("test3", "test3", "testPassword", calendar.getTime(),address);
        accountHolder1.setId((long) 3);
        List<AccountHolder> accountHolderList = Arrays.asList(accountHolder1, accountHolder2, accountHolder3);
        when(accountHolderRepository.findAll()).thenReturn(accountHolderList);
        when(accountHolderRepository.findById((long) 1)).thenReturn(Optional.of(accountHolder1));
        when(accountHolderRepository.findById((long) 2)).thenReturn(Optional.of(accountHolder2));
        when(accountHolderRepository.findById((long) 3)).thenReturn(Optional.of(accountHolder3));
        when(accountHolderRepository.save(accountHolder1)).thenReturn(accountHolder1);
        when(accountHolderRepository.save(accountHolder2)).thenReturn(accountHolder2);
        Role role = new Role();
        when(roleRepository.save(role)).thenReturn(null);
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_ValidAccountHolderNoMailingAddress_AuthAdmin() {
        when(accountHolderRepository.save(Mockito.any(AccountHolder.class))).thenReturn(accountHolder1);
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder1);
        assertEquals(accountHolder1.getDateOfBirth(), newAccountHolder.getDateOfBirth());
        assertEquals(accountHolder1.getPrimaryAddress(), newAccountHolder.getPrimaryAddress());
        assertEquals(null, newAccountHolder.getMailingAddress());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_ValidAccountHolderWithMailingAddress_AuthAdmin() {
        when(accountHolderRepository.save(Mockito.any(AccountHolder.class))).thenReturn(accountHolder2);
        AccountHolder newAccountHolder = accountHolderService.create(accountHolder2);
        assertEquals(accountHolder2.getDateOfBirth(), newAccountHolder.getDateOfBirth());
        assertEquals(accountHolder2.getPrimaryAddress(), newAccountHolder.getPrimaryAddress());
        assertEquals(accountHolder2.getMailingAddress(), newAccountHolder.getMailingAddress());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void Create_RepeatedUsernameAccountHolder_ThrowException() {
        when(accountHolderRepository.save(Mockito.any(AccountHolder.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DuplicatedUsernameException.class, () -> accountHolderService.create(accountHolder2));
    }

    @Test
    public void Create_ValidAccountHolder_NoAuth() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()-> accountHolderService.create(accountHolder1));
    }

    @Test
    public void findById_ValidId_AccountHolder() {
        AccountHolder result = accountHolderService.findById((long) 1);
        assertEquals("test1", result.getName());
        assertEquals("test1", result.getUsername());
        assertEquals(address, result.getPrimaryAddress());
    }

    @Test
    public void findById_NOTValidId_AccountHolder() {
        assertThrows(NoSuchAccountHolderException.class, () -> accountHolderService.findById((long) 4));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void findAll() {
        List<AccountHolder> accountHolderList = accountHolderService.findAll();
        assertEquals(3, accountHolderList.size());
        for (AccountHolder accountHolder: accountHolderList) {
            assertEquals(address, accountHolder.getPrimaryAddress());
        }
    }

    @Test
    public void login_notLogged() {
        accountHolder1.logout();
        when(accountHolderRepository.findById(accountHolder1.getId())).thenReturn(Optional.of(accountHolder1));
        accountHolderService.login(accountHolder1);
        assertTrue(accountHolder1.isLoggedIn());
        accountHolder1.logout();
    }

    @Test
    public void login_AlreadyLogged() {
        when(accountHolderRepository.findById(Mockito.anyLong())).thenThrow(UserAlreadyLoggedInException.class);
        assertThrows(UserAlreadyLoggedInException.class, () -> accountHolderService.login(accountHolder1));
    }

    @Test
    public void logout_loggedIn() {
        accountHolder1.login();
        when(accountHolderRepository.findById(accountHolder1.getId())).thenReturn(Optional.of(accountHolder1));
        accountHolderService.logout(accountHolder1);
        assertTrue(!accountHolder1.isLoggedIn());
        accountHolder1.login();
    }

    @Test
    public void logout_alreadyLoggedOut() {
        when(accountHolderRepository.findById(Mockito.anyLong())).thenThrow(UserNotLoggedInException.class);
        assertThrows(UserNotLoggedInException.class, () -> accountHolderService.logout(accountHolder1));
    }
}