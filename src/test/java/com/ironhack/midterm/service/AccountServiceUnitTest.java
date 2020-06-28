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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    @MockBean
    private AccountHolderRepository accountHolderRepository;

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
    ThirdParty thirdParty;
    ThirdPartyOperationDTO operationDTO = new ThirdPartyOperationDTO();


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
        thirdParty = new ThirdParty("third-party", "third-party");
        thirdParty.setId((long) 9);
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
        accountHolder1.login();
        List<Account> accountHolder1Accounts = Stream.of(checking1, checking3, savings1, creditCard1, studentChecking1).collect(Collectors.toList());
        when(accountHolderRepository.findById(accountHolder1.getId())).thenReturn(Optional.of(accountHolder1));
        when(accountRepository.findById(checking1.getId())).thenReturn(Optional.of(checking1));
        when(accountRepository.findById(checking2.getId())).thenReturn(Optional.of(checking2));
        when(accountRepository.findById(creditCard1.getId())).thenReturn(Optional.of(creditCard1));
        when(accountRepository.findById(savings1.getId())).thenReturn(Optional.of(savings1));
        when(accountRepository.findById(studentChecking1.getId())).thenReturn(Optional.of(studentChecking1));
        when(accountRepository.findByPrimaryOwnerId(accountHolder1.getId())).thenReturn(accountHolder1Accounts);
        when(userRepository.findById(accountHolder1.getId())).thenReturn(Optional.of(accountHolder1));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(thirdPartyRepository.findByHashedKey(thirdParty.getHashedKey())).thenReturn(Optional.of(thirdParty));
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
        when(accountHolderRepository.findById(accountHolder2.getId())).thenReturn(Optional.of(accountHolder2));
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
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("test3");
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
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("test3");
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
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("test3");
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
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("test3");
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
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_BadUserNameAndSecondOwner_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        checking2.setSecondaryOwner(accountHolder2);
        assertThrows(NoPermissionForUserException.class, () -> accountService.transfer(checking2.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_AccountBlocked_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        checking1.setStatus(AccountStatus.FROZEN);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        List<Transaction> previousTransactions = new ArrayList<>();
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        previousTransactions.add(transaction);
        when(transactionRepository.findTransactionOneSecondAgo(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(previousTransactions);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_MaxNumberOfClientTransactionsNullAndCurrentNumberMoreThan2_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(3.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTHOLDER"})
    public void transfer_MaxNumberOfClientTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        transferDTO.setAmount(amount);
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("notfound");
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

    @Test
    public void transfer_NotAuth_ThrowsException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> accountService.transfer(checking1.getId(), accountHolder1, transferDTO));
    }

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

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_FrozenAccount_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        checking1.setStatus(AccountStatus.FROZEN);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        List<Transaction> previousTransactions = new ArrayList<>();
        previousTransactions.add(transaction);
        when(transactionRepository.findTransactionOneSecondAgo(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(previousTransactions);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_MaxNumberOfOwnerTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_MaxNumberOfOwnerTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);

        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_MaxNumberOfAdminTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_MaxNumberOfAdminTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);

        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(checking1.getId(), admin, amountDTO));
    }


    @Test
    public void creditAccount_ThirdPartyToCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("3000.00"), checking1.getBalance().getAmount());
    }


    @Test
    public void creditAccount_ThirdPartyToSavingsAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(savings1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(savings1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(thirdParty.getHashedKey(), savings1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("3000.00"), savings1.getBalance().getAmount());
    }

    @Test
    public void creditAccount_ThirdPartyToCreditCardAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("100");
        operationDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(thirdParty.getHashedKey(), creditCard1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("-100.00"), creditCard1.getBalance().getAmount());
    }

    @Test
    public void creditAccount_ThirdPartyToStudentCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(studentChecking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(studentChecking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.creditAccount(thirdParty.getHashedKey(), studentChecking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("3000.00"), studentChecking1.getBalance().getAmount());
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
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    public void creditAccountThirdParty_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        List<Transaction> previousTransactions = new ArrayList<>();
        previousTransactions.add(transaction);
        when(transactionRepository.findTransactionOneSecondAgo(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(previousTransactions);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfOwnerTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfOwnerTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);

        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfAdminTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void creditAccount_ThirdPartyMaxNumberOfAdminTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.creditAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

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

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_FrozenAccount_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        checking1.setStatus(AccountStatus.FROZEN);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        List<Transaction> previousTransactions = new ArrayList<>();
        previousTransactions.add(transaction);
        when(transactionRepository.findTransactionOneSecondAgo(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(previousTransactions);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_MaxNumberOfOwnerTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_MaxNumberOfOwnerTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);

        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_MaxNumberOfAdminTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_MaxNumberOfAdminTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(checking1.getId(), admin, amountDTO));
    }

    @Test
    public void debitAccount_ThirdPartyToCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), checking1.getBalance().getAmount());
    }


    @Test
    public void debitAccount_ThirdPartyToSavingsAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(savings1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(savings1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), savings1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), savings1.getBalance().getAmount());
    }

    @Test
    public void debitAccount_ThirdPartyToCreditCardAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("100");
        operationDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(creditCard1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), creditCard1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("100.00"), creditCard1.getBalance().getAmount());
    }

    @Test
    public void debitAccount_ThirdPartyToStudentCheckingAccount_accountCredited() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(studentChecking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(studentChecking1);
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        TransactionComplete result = accountService.debitAccount(thirdParty.getHashedKey(), studentChecking1.getId(), operationDTO);
        assertEquals(thirdParty.getId(),result.getTransactionMakerId());
        assertNull(result.getUserAccount());
        assertEquals(new BigDecimal("1000.00"), studentChecking1.getBalance().getAmount());
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
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        checking1.setStatus(AccountStatus.FROZEN);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    public void debitAccountThirdParty_PreviousTransaction_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        operationDTO.setAmount(amount);
        operationDTO.setAccountSecretKey(checking1.getSecretKey());
        Transaction transaction = new Transaction(new Money(amount), new Date(), thirdParty);
        transaction.setCreditedAccount(checking1);
        List<Transaction> previousTransactions = new ArrayList<>();
        previousTransactions.add(transaction);
        when(transactionRepository.findTransactionOneSecondAgo(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(previousTransactions);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_ThirdPartyMaxNumberOfOwnerTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_ThirdPartyMaxNumberOfOwnerTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);

        when(transactionRepository.findCurrentDateTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfOwner(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_ThirdPartyMaxNumberOfAdminTransactionsNullAndCurrentNumberMax_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(null);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void debitAccount_ThirdPartyMaxNumberOfAdminTransactionsSmallerThanCurrentNumber_ThrowsException() {
        BigDecimal amount = new BigDecimal("1000");
        amountDTO.setAmount(amount);
        Transaction transaction = new Transaction(new Money(amount), new Date(), admin);
        transaction.setCreditedAccount(checking1);
        when(transactionRepository.findCurrentDateTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(4.0);
        when(transactionRepository.findHighestTotalTransactionCountOfUser(Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(2.0);
        assertThrows(FraudDetectionException.class, () -> accountService.debitAccount(thirdParty.getHashedKey(), checking1.getId(), operationDTO));
    }

}