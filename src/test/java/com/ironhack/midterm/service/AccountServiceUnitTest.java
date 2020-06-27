package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.NoPermissionForUserException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class AccountServiceUnitTest {
    @Autowired
    private AccountService accountService;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private CreditCardRepository creditCardRepository;
    @MockBean
    private CheckingRepository checkingRepository;
    @MockBean
    private StudentCheckingRepository studentCheckingRepository;
    @MockBean
    private SavingsRepository savingsRepository;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ThirdPartyRepository thirdPartyRepository;

    AccountHolder accountHolder1;
    AccountHolder accountHolder2;
    Checking checking1;
    Savings savings1;
    CreditCard creditCard1;
    Checking checking2;
    Checking checking3;

    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1990, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder1 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        accountHolder1.setId((long)1);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(), address);
        accountHolder2.setId((long)2);
        checking1 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking1.setId((long) 1);
        checking3 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking3.setId((long) 3);
        savings1 = new Savings(new Money(new BigDecimal("2000")), accountHolder1);
        savings1.setId((long) 4);
        creditCard1 = new CreditCard(accountHolder1);
        creditCard1.setId((long) 5);
        List<Account> accountHolder1Accounts = Stream.of(checking1, checking3, savings1, creditCard1).collect(Collectors.toList());
        when(accountRepository.findById(checking1.getId())).thenReturn(Optional.of(checking1));
        when(accountRepository.findById(creditCard1.getId())).thenReturn(Optional.of(creditCard1));
        when(accountRepository.findById(savings1.getId())).thenReturn(Optional.of(savings1));

        when(accountRepository.findByPrimaryOwnerId(accountHolder1.getId())).thenReturn(accountHolder1Accounts);

    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountChecking_GetBalance() {
        AccountBalance result = accountService.getBalanceById(checking1.getId(),accountHolder1);
        assertEquals(checking1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountCreditCard_GetBalance() {
        AccountBalance result = accountService.getBalanceById(creditCard1.getId(),accountHolder1);
        assertEquals(creditCard1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountSavings_GetBalance() {
        AccountBalance result = accountService.getBalanceById(savings1.getId(),accountHolder1);
        assertEquals(savings1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdChecking_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(checking1.getId());
        assertEquals(checking1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdCredit_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(creditCard1.getId());
        assertEquals(creditCard1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdSavings_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(savings1.getId());
        assertEquals(savings1.getBalance(), result.getBalance());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_NOTOwnerOfAccount_ThrowException() {
        assertThrows(NoPermissionForUserException.class, () -> accountService.getBalanceById(checking1.getId(),accountHolder2) );
    }

    @Test
    public void getBalanceById_NoUserLogged_ThrowException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.getBalanceById(checking1.getId(),accountHolder2) );
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getAllBalanceByUserId_Accounts_GetBalanceList() {
        List<AccountBalance> results = accountService.getAllBalanceByUserId(accountHolder1);
        assertEquals(4, results.size());
        for (AccountBalance account : results) {
            assertTrue(account.getBalance().getAmount().compareTo(new BigDecimal("-1")) > 0);
        }
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getAllBalanceByUserId_NoAccounts_ThrowException() {
        assertThrows(NoSuchAccountException.class, () -> accountService.getAllBalanceByUserId(accountHolder2) );
    }

    @Test
    public void getAllBalanceByUserId_NoUserLogged_ThrowException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.getAllBalanceByUserId(accountHolder2) );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllBalanceByUserId_UserWithNoAccount_GetBalanceList() {
        List<AccountBalance> results = accountService.getAllBalanceByUserId(accountHolder1.getId());
        assertEquals(4, results.size());
        for (AccountBalance account : results) {
            assertTrue(account.getBalance().getAmount().compareTo(new BigDecimal("-1")) > 0);
        }
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllBalanceByUserId_UserWithNoAccount_ThrowException() {
        assertThrows(NoSuchAccountException.class, () -> accountService.getAllBalanceByUserId(accountHolder2.getId()) );
    }



    @Test
    public void getAllBalanceByUserIdADMIN_NoUserLogged_ThrowException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.getAllBalanceByUserId(accountHolder2.getId()) );
    }

    public void transfer() {}
    public void creditAccount() {}
    public void debitAccount() {}
}