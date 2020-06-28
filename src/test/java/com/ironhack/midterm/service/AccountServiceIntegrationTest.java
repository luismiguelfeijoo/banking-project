package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.enums.AccountStatus;
import com.ironhack.midterm.exceptions.*;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceIntegrationTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private CheckingRepository checkingRepository;
    @Autowired
    private StudentCheckingRepository studentCheckingRepository;
    @Autowired
    private SavingsRepository savingsRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
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
    SecuredUser accountHolder;
    ThirdParty thirdParty;
    ThirdPartyOperationDTO operationDTO = new ThirdPartyOperationDTO();


    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1990, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder1 = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        accountHolder1 = accountHolderRepository.save(accountHolder1);
        accountHolder2 = new AccountHolder("test2", "test2", "testPassword", calendar.getTime(), address);
        accountHolder2 = accountHolderRepository.save(accountHolder2);
        accountHolder3 = new AccountHolder("test3", "test3", "testPassword", calendar.getTime(), address);
        accountHolder3 = accountHolderRepository.save(accountHolder3);
        //accountHolder = new AccountHolder("accountHolder", "admin", "admin", calendar.getTime(), address);
        thirdParty = new ThirdParty("third-party", "third-party");
        thirdParty = thirdPartyRepository.save(thirdParty);
        checking1 = accountRepository.save(new Checking(new Money(new BigDecimal("2000")), accountHolder1));
        checking2 = new Checking(new Money(new BigDecimal("2000")), accountHolder2);
        checking2.setPrimaryOwner(accountHolder3);
        accountRepository.save(checking2);
        checking3 = new Checking(new Money(new BigDecimal("2000")), accountHolder1);
        checking3 = checkingRepository.save(checking3);
        savings1 = new Savings(new Money(new BigDecimal("2000")), accountHolder1);
        savings1 = savingsRepository.save(savings1);
        creditCard1 = new CreditCard(accountHolder1);
        creditCard1 = creditCardRepository.save(creditCard1);
        accountHolder1.login();
        accountHolderRepository.save(accountHolder1);
        studentChecking1 = new StudentChecking(new Money(new BigDecimal("2000")), accountHolder1);
        studentChecking1 = studentCheckingRepository.save(studentChecking1);
    }

    @AfterEach
    public void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        accountHolderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountChecking_GetBalance() {
        AccountBalance result = accountService.getBalanceById(checking1.getId(),accountHolder1);
        assertEquals(checking1.getBalance().getAmount(), result.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountCreditCard_GetBalance() {
        AccountBalance result = accountService.getBalanceById(creditCard1.getId(),accountHolder1);
        assertEquals(creditCard1.getBalance().getAmount(), result.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_OwnerOfAccountSavings_GetBalance() {
        AccountBalance result = accountService.getBalanceById(savings1.getId(),accountHolder1);
        assertEquals(savings1.getBalance().getAmount(), result.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdChecking_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(checking1.getId());
        assertEquals(checking1.getBalance().getAmount(), result.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdCredit_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(creditCard1.getId());
        assertEquals(creditCard1.getBalance().getAmount(), result.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getBalanceByIdSavings_ADMIN_GetBalance() {
        AccountBalance result = accountService.getBalanceById(savings1.getId());
        assertEquals(savings1.getBalance().getAmount(), result.getBalance().getAmount());
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
    public void getAllBalanceByUserId_NoAccounts_EmptyList() {
        accountRepository.deleteAll();
        accountHolder3.login();
        accountHolderRepository.save(accountHolder3);
        List<AccountBalance> results = accountService.getAllBalanceByUserId(accountHolder3);
        assertEquals(0, results.size());
        //assertThrows(NoSuchAccountException.class, () -> accountService.getAllBalanceByUserId(accountHolder2) );
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getAllBalance_NotLoggedIn_EmptyList() {
        accountHolder1.logout();
        accountHolderRepository.save(accountHolder1);
        assertThrows(NoPermissionForUserException.class, () -> accountService.getAllBalanceByUserId(accountHolder1) );
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void getBalanceById_NotLoggedIn_EmptyList() {
        accountHolder1.logout();
        accountHolderRepository.save(accountHolder1);
        assertThrows(NoPermissionForUserException.class, () -> accountService.getBalanceById(checking1.getId(), accountHolder1) );
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_NotLoggedIn_EmptyList() {
        accountHolder1.logout();
        accountHolderRepository.save(accountHolder1);
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
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
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("test3");
        TransactionComplete result = accountService.transfer(checking1.getId(), accountHolder1, transferDTO);
        checking1 = checkingRepository.findById(checking1.getId()).get();
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("1000"), result.getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromSavingsAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("test3");
        TransactionComplete result = accountService.transfer(savings1.getId(), accountHolder1, transferDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass() );
        savings1 = savingsRepository.findById(savings1.getId()).get();
        accountHolder1.login();
        accountHolderRepository.save(accountHolder1);
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromCreditCardAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("100");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("test3");
        TransactionComplete result = accountService.transfer(creditCard1.getId(), accountHolder1, transferDTO);
        creditCard1 = creditCardRepository.findById(creditCard1.getId()).get();
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_FromStudentCheckingAccount_TransferMade() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("test3");
        TransactionComplete result = accountService.transfer(studentChecking1.getId(), accountHolder1, transferDTO);
        studentChecking1 = studentCheckingRepository.findById(studentChecking1.getId()).get();
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_BadUserNameAndNoSecondOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("notfound");
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_BadUserNameAndSecondOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("notfound");
        checking2.setSecondaryOwner(accountHolder2);
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking2.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_AccountBlocked_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("notfound");
        checking1.setStatus(AccountStatus.FROZEN);
        checkingRepository.save(checking1);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }


    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_PreviousTransaction_ThrowsException() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking3.getId());
        transferDTO.setReceiverName("test1");
        Transaction transaction = new Transaction(new Money(amount), calendar.getTime(), accountHolder1);
        transaction.setTransactionMaker(accountHolder1);
        transaction.setCreditedAccount(checking1);
        transactionRepository.save(transaction);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_MaxNumberOfClientTransactions_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId(checking2.getId());
        transferDTO.setReceiverName("test2");
        Transaction transaction1 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction1.setTransactionMaker(accountHolder1);
        transaction1.setCreditedAccount(checking1);
        Transaction transaction2 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction2.setTransactionMaker(accountHolder1);
        transaction2.setCreditedAccount(checking1);
        transactionRepository.saveAll(Stream.of(transaction1, transaction2).collect(Collectors.toList()));
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    public void transfer_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_Admin_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setCreditedAccount(checking1);
        TransactionComplete result = accountService.creditAccount(checking1.getId(), accountHolder1, amountDTO);
        checking1 = checkingRepository.findById(checking1.getId()).get();
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(AccountBalance.class, result.getUserAccount().getClass());
        assertEquals(new BigDecimal("3000.00"), checking1.getBalance().getAmount());
    }



    @Test
    public void creditAccount_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.creditAccount(checking1.getId(), accountHolder1, amountDTO));
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_MaxNumberOfAdminTransactions_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction1 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction1.setCreditedAccount(checking1);
        Transaction transaction2 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction2.setCreditedAccount(checking1);
        transactionRepository.saveAll(Stream.of(transaction1,transaction2).collect(Collectors.toList()));
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), accountHolder1, amountDTO));
    }


    @Test
    public void creditAccount_ThirdPartyToCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        TransactionComplete result = accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("3000.00"), checking1.getBalance().getAmount().add(new BigDecimal("1000")));
    }



    @Test
    public void creditAccount_BadHashedKey_ThrowsException() {
        assertThrows(NoSuchThirdPartyException.class, () -> accountService.creditAccount(UUID.randomUUID(), checking1.getId(), operationDTO));
    }

    @Test
    public void creditAccountThirdParty_FrozenAccount_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        checking1.setStatus(AccountStatus.FROZEN);
        checkingRepository.save(checking1);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    public void creditAccountThirdParty_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        transactionRepository.save(transaction);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfOwnerTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction1 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction1.setTransactionMaker(accountHolder1);
        transaction1.setCreditedAccount(checking1);
        Transaction transaction2 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction2.setTransactionMaker(accountHolder1);
        transaction2.setCreditedAccount(checking1);
        transactionRepository.saveAll(Stream.of(transaction1, transaction2).collect(Collectors.toList()));
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfAdminTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder);
        transaction.setCreditedAccount(checking1);
        Transaction transaction1 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction1.setTransactionMaker(accountHolder1);
        transaction1.setCreditedAccount(checking1);
        Transaction transaction2 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction2.setTransactionMaker(accountHolder1);
        transaction2.setCreditedAccount(checking1);
        transactionRepository.saveAll(Stream.of(transaction1, transaction2).collect(Collectors.toList()));
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToCheckingAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        TransactionComplete result = accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("1000.00"), checking1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToSavingsAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(savings1);
        TransactionComplete result = accountService.debitAccount(savings1.getId(), accountHolder1, amountDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToCreditCardAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("100");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setDebitedAccount(creditCard1);
        TransactionComplete result = accountService.debitAccount(creditCard1.getId(), accountHolder1, amountDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount().add(new BigDecimal("100")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_AdminToStudentCheckingAccount_accountDebited() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder);
        transaction.setDebitedAccount(studentChecking1);
        TransactionComplete result = accountService.debitAccount(studentChecking1.getId(), accountHolder1, amountDTO);
        assertEquals(accountHolder1.getId(),result.getTransactionMakerId());
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_BadUserNameID_ThrowsException() {
        accountHolder1.setId((long) 10000000);
        assertThrows(NoSuchUserException.class, () -> accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO));
    }


    @Test
    public void debitAccount_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_FrozenAccount_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        checking1.setStatus(AccountStatus.FROZEN);
        checkingRepository.save(checking1);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction.setCreditedAccount(checking1);
        transactionRepository.save(transaction);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), accountHolder1, amountDTO));
    }

    @Test
    public void debitAccount_ThirdPartyToCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), checking1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }

    @Test
    public void debitAccount_ThirdPartyToSavingsAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(savings1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(savings1);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), savings1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }

    @Test
    public void debitAccount_ThirdPartyToCreditCardAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("100");
        operationDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(creditCard1);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), creditCard1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount().add(new BigDecimal("100")));
    }

    @Test
    public void debitAccount_ThirdPartyToStudentCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(studentChecking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(studentChecking1);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), studentChecking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount().subtract(new BigDecimal("1000")));
    }


    @Test
    public void debitAccount_BadHashedKey_ThrowsException() {
        assertThrows(NoSuchThirdPartyException.class, () -> accountService.debitAccount(UUID.randomUUID(), checking1.getId(), operationDTO));
    }

    @Test
    public void debitAccountThirdParty_FrozenAccount_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        checking1.setStatus(AccountStatus.FROZEN);
        checkingRepository.save(checking1);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    public void debitAccountThirdParty_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        transactionRepository.save(transaction);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_ThirdPartyMaxNumberOfOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction1 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction1.setCreditedAccount(checking1);
        Transaction transaction2 = new Transaction(new Money(amount), new Date(), accountHolder1);
        transaction2.setCreditedAccount(checking1);
        transactionRepository.saveAll(Stream.of(transaction1, transaction2).collect(Collectors.toList()));
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }







}