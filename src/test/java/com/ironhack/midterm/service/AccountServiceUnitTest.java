package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.exceptions.NoPermissionForUserException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.exceptions.NoSuchUserException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.*;
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
    AccountHolder accountHolder3;
    Checking checking1;
    StudentChecking studentChecking1;
    Savings savings1;
    CreditCard creditCard1;
    Checking checking2;
    Checking checking3;
    TransferDTO transferDTO = new TransferDTO();
    AmountDTO amountDTO = new AmountDTO();
    SecuredUser admin;


    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1990, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder1 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        accountHolder1.setId((long)1);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(), address);
        accountHolder2.setId((long)2);
        accountHolder3 = new AccountHolder("test3", "test3", "testPassword", calendar.getTime(), address);
        admin = new AccountHolder("admin", "admin", "admin", calendar.getTime(), address);
        admin.setId((long) 99);
        accountHolder3.setId((long)3);
        checking1 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking1.setId((long) 1);
        checking2 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking2.setId((long) 2);
        checking2.setPrimaryOwner(accountHolder3);
        checking3 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking3.setId((long) 3);
        savings1 = new Savings(new Money(new BigDecimal("2000")), accountHolder1);
        savings1.setId((long) 4);
        creditCard1 = new CreditCard(accountHolder1);
        creditCard1.setId((long) 5);
        studentChecking1 = new StudentChecking(new Money(new BigDecimal("2000")), accountHolder1);
        studentChecking1.setId((long) 6);

        List<Account> accountHolder1Accounts = Stream.of(checking1, checking3, savings1, creditCard1, studentChecking1).collect(Collectors.toList());
        when(accountRepository.findById(checking1.getId())).thenReturn(Optional.of(checking1));
        when(accountRepository.findById(checking2.getId())).thenReturn(Optional.of(checking2));
        when(accountRepository.findById(creditCard1.getId())).thenReturn(Optional.of(creditCard1));
        when(accountRepository.findById(savings1.getId())).thenReturn(Optional.of(savings1));
        when(accountRepository.findById(studentChecking1.getId())).thenReturn(Optional.of(studentChecking1));
        when(accountRepository.findByPrimaryOwnerId(accountHolder1.getId())).thenReturn(accountHolder1Accounts);
        when(userRepository.findById(accountHolder1.getId())).thenReturn(Optional.of(accountHolder1));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
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
        assertEquals(5, results.size());
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
        assertEquals(5, results.size());
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

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromCheckingAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("test3");
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(checking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.transfer(checking1.getId(), accountHolder1, transferDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass() );
        assertEquals(new BigDecimal("1000.00"), checking1.getBalance().getAmount());
        assertEquals(new BigDecimal("3000.00"), checking2.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromSavingsAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("test3");
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(savings1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.transfer(savings1.getId(), accountHolder1, transferDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass() );
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount());
        assertEquals(new BigDecimal("3000.00"), checking2.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromCreditCardAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("100");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("test3");
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.transfer(creditCard1.getId(), accountHolder1, transferDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass() );
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount());
        assertEquals(new BigDecimal("2100.00"), checking2.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromStudentCheckingAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("test3");
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.transfer(studentChecking1.getId(), accountHolder1, transferDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass() );
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount());
        assertEquals(new BigDecimal("3000.00"), checking2.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_BadUserNameAndNoSecondOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("notfound");
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_BadUserNameAndSecondOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setRecieverAccountId((long) 2);
        transferDTO.setRecieverName("notfound");
        checking1.setSecondaryOwner(accountHolder2);
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    public void transfer_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    public void creditAccount() {}

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_AdminToCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(checking1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("3000.00"), checking1.getBalance().getAmount());
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_AdminToSavingsAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(savings1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(savings1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("3000.00"), savings1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_AdminToCreditCardAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("100");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(creditCard1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("-100.00"), creditCard1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_AdminToStudentCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(studentChecking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(studentChecking1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("3000.00"), studentChecking1.getBalance().getAmount());
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_BadUserNameID_ThrowsException() {
        assertThrows(NoSuchUserException.class, () -> accountService.creditAccount(checking1.getId(), new AccountHolder(), amountDTO));
    }


    @Test
    public void creditAccount_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.creditAccount(checking1.getId(), accountHolder1, amountDTO));
    }


    public void debitAccount() {}

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToCheckingAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setDebitedAccount(checking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(checking1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("1000.00"), checking1.getBalance().getAmount());
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToSavingsAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setDebitedAccount(savings1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(savings1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToCreditCardAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("100");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setDebitedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(creditCard1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToStudentCheckingAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setDebitedAccount(studentChecking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(studentChecking1.getId(), admin, amountDTO);
        assertEquals(admin.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount());
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_BadUserNameID_ThrowsException() {
        assertThrows(NoSuchUserException.class, () -> accountService.debitAccount(checking1.getId(), new AccountHolder(), amountDTO));
    }


    @Test
    public void debitAccount_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO));
    }
}