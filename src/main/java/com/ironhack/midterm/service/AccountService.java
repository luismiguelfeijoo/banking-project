package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.enums.AccountStatus;
import com.ironhack.midterm.exceptions.*;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {
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

    private final static Logger LOGGER = LogManager.getLogger(AccountService.class);

    @Secured({"ROLE_ACCOUNTHOLDER"})
    public AccountBalance getBalanceById(Long accountId, SecuredUser securedUser) {
        AccountHolder user = accountHolderRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
        LOGGER.info("[ACCOUNT ACCESS INIT (user)] - UserId: " + securedUser.getId() + " - AccountId: " + accountId);
        if (!user.isLoggedIn()) throw new NoPermissionForUserException("You must be logged in to access this page");
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account instanceof CreditCard) {
            ((CreditCard) account).applyInterestRate();
        } else if (account instanceof Savings) {
            ((Savings) account).applyInterestRate();
            ((Savings) account).applyMaintenanceFee();
        } else if (account instanceof Checking) {
            ((Checking) account).applyMaintenanceFee();
        }
        if (account.hasAccess(securedUser.getId())) {
            LOGGER.info("[ACCOUNT ACCESS GRANTED (user)] - UserId: " + securedUser.getId() + " - AccountId:" + accountId + " - AccountType:" + account.getClass());
            return new AccountBalance(account.getBalance());
        } else {
            LOGGER.info("[ACCOUNT ACCESS DENIED (user)] - UserId:" + securedUser.getId() + " - AccountId:" + accountId);
            throw new NoPermissionForUserException("You don't have permission");
        }
    }

    @Secured({"ROLE_ADMIN"})
    public AccountBalance getBalanceById(Long accountId) {
        LOGGER.info("[ACCOUNT ACCESS INIT (admin)] - AccountId:" + accountId);
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account instanceof CreditCard) {
            ((CreditCard) account).applyInterestRate();
        } else if (account instanceof Savings) {
            ((Savings) account).applyInterestRate();
            ((Savings) account).applyMaintenanceFee();
        } else if (account instanceof Checking) {
            ((Checking) account).applyMaintenanceFee();
        }
        LOGGER.info("[ACCOUNT ACCESS GRANTED (admin)] - AccountId:" + accountId + " - AccountType:" + account.getClass());
        return new AccountBalance(account.getBalance());
    }

    @Secured({"ROLE_ACCOUNTHOLDER"})
    public List<AccountBalance> getAllBalanceByUserId(SecuredUser securedUser) {
        AccountHolder user = accountHolderRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
        LOGGER.info("[ALL ACCOUNTS ACCESS INIT (user)] - UserId: " + securedUser.getId());
        if (!user.isLoggedIn()) throw new NoPermissionForUserException("You must be logged in to access this page");
        List<Account> accounts = accountRepository.findByPrimaryOwnerId(securedUser.getId());
        //if (accounts.size() == 0) throw new NoSuchAccountException("User doesn't have registered accounts");
        return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
    }

    @Secured({"ROLE_ADMIN"})
    public List<AccountBalance> getAllBalanceByUserId(Long userId) {
        LOGGER.info("[ALL ACCOUNTS ACCESS INIT (admin)] - OwnerId: " + userId);
        List<Account> accounts = accountRepository.findByPrimaryOwnerId(userId);
        if (accounts.size() == 0) throw new NoSuchAccountException("User doesn't have registered accounts");
        LOGGER.info("[ALL ACCOUNTS ACCESS GRANTED (admin)] - OwnerId: " + userId);
        return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
    }

    @Secured({"ROLE_ACCOUNTHOLDER"})
    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete transfer(Long accountId, SecuredUser securedUser, TransferDTO transferDTO) {
        AccountHolder user = accountHolderRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
        LOGGER.info("[TRANSFER INIT] - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
        if (!user.isLoggedIn()) throw new NoPermissionForUserException("You must be logged in to access this page");
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (!account.hasAccess(securedUser.getId())) throw new NoPermissionForUserException("You don't have permission to do this transfer");
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            LOGGER.info("[TRANSFER POSSIBLE FRAUD DETECTED] Message: two transfers in 1 second period - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                LOGGER.info("[TRANSFER POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId()+ " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                LOGGER.info("[TRANSFER POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId()+ " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Account receiverAccount = accountRepository.findById(transferDTO.getReceiverAccountId()).orElseThrow(() -> new NoSuchAccountException("There's no reciever account with provided ID"));
        if (!receiverAccount.getPrimaryOwner().getName().equals(transferDTO.getReceiverName())) {
            if (receiverAccount.getSecondaryOwner() == null)  {
                LOGGER.info("[TRANSFER DENIED NO PERMISSION] - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
                throw new NoPermissionForUserException("The user doesn't correspond with the owner of the account");
            } else if (!receiverAccount.getSecondaryOwner().getName().equals(transferDTO.getReceiverName())){
                LOGGER.info("[TRANSFER DENIED NO PERMISSION] - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
                throw new NoPermissionForUserException("The user doesn't correspond with the owner of the account");
            }
        }
        //User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        Transaction transaction = new Transaction(new Money(transferDTO.getAmount()), new Date(), user);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.debitAccount(new Money(transferDTO.getAmount()));
            transaction.setDebitedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            savings.debitAccount(new Money(transferDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setDebitedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            studentChecking.debitAccount(new Money(transferDTO.getAmount()));
            transaction.setDebitedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            checking.debitAccount(new Money(transferDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setDebitedAccount(checking);
        }
        LOGGER.info("[TRANSFER ACCEPTED BEFORE SAVING] - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
        receiverAccount.creditAccount(new Money(transferDTO.getAmount()));
        transaction.setCreditedAccount(receiverAccount);
        Transaction doneTransaction = transactionRepository.save(transaction);
        LOGGER.info("[TRANSFER ACCEPTED AND SAVED] - UserId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + transferDTO.getAmount() + " - ReceiverAccountId: " + transferDTO.getReceiverAccountId());
        TransactionComplete response = new TransactionComplete();
        response.setAmount(transferDTO.getAmount());
        response.setTransactionMakerId(user.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getDebitedAccount().getBalance()));
        return response;
    }

    @Secured({"ROLE_ADMIN"})
    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete creditAccount(Long accountId, SecuredUser securedUser, AmountDTO amountDTO) {
        LOGGER.info("[CREDIT ACCOUNT INIT(admin)] - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED (admin)] Message: two transfers in 1 second period - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED (admin)] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED (admin)] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(securedUser.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(securedUser.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED (admin)] Message: max daily amount for admin reached - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED (admin)] Message: max daily amount for admin reached - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Transaction transaction = new Transaction(new Money(amountDTO.getAmount()), new Date(), transactionMaker);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.creditAccount(new Money(amountDTO.getAmount()));
            transaction.setCreditedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            savings.creditAccount(new Money(amountDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setCreditedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            studentChecking.creditAccount(new Money(amountDTO.getAmount()));
            transaction.setCreditedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            checking.creditAccount(new Money(amountDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setCreditedAccount(checking);
        }
        LOGGER.info("[CREDIT ACCOUNT ACCEPTED BEFORE SAVING (admin)] - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        Transaction doneTransaction = transactionRepository.save(transaction);
        LOGGER.info("[CREDIT ACCOUNT ACCEPTED SAVED (admin)] - AdminId: " + securedUser.getId() + " - AccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        TransactionComplete response = new TransactionComplete();
        response.setAmount(amountDTO.getAmount());
        response.setTransactionMakerId(transactionMaker.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getCreditedAccount().getBalance()));
        return response;
    }

    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete creditAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO operationDTO) {
        LOGGER.info("[CREDIT ACCOUNT INIT(third-party)] - hashedKey: " + hashedKey + " - AccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
        ThirdParty thirdParty = thirdPartyRepository.findByHashedKey(hashedKey).orElseThrow(() -> new NoSuchThirdPartyException("There's no third-party with provided credentials"));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: two transfers in 1 second period - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for account reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for account reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(thirdParty.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(thirdParty.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
                LOGGER.info("[CREDIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Transaction transaction = new Transaction(new Money(operationDTO.getAmount()), new Date(), thirdParty);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.creditAccount(new Money(operationDTO.getAmount()));
            transaction.setCreditedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            if (!savings.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            savings.creditAccount(new Money(operationDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setCreditedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            if (!studentChecking.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            studentChecking.creditAccount(new Money(operationDTO.getAmount()));
            transaction.setCreditedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            if (!checking.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            checking.creditAccount(new Money(operationDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setCreditedAccount(checking);
        }
        LOGGER.info("[CREDIT ACCOUNT ACCEPTED BEFORE SAVE (third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
        Transaction doneTransaction = transactionRepository.save(transaction);
        LOGGER.info("[CREDIT ACCOUNT POSSIBLE AND SAVED (third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());

        TransactionComplete response = new TransactionComplete();
        response.setAmount(operationDTO.getAmount());
        response.setTransactionMakerId(thirdParty.getId());
        // Not shown for not giving info to third party
        //response.setUserAccount(new AccountBalance(transaction.getCreditedAccount().getBalance()));
        return response;
    }

    @Secured({"ROLE_ADMIN"})
    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete debitAccount(Long accountId, SecuredUser securedUser, AmountDTO amountDTO) {
        LOGGER.info("[DEBIT ACCOUNT INIT(admin)] - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            LOGGER.info("[DEBIT ACCOUNT FRAUD DETECTED] Message: two transfers in 1 second period - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(securedUser.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(securedUser.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED] Message: max daily amount for account reached - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount()  + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Transaction transaction = new Transaction(new Money(amountDTO.getAmount()), new Date(), transactionMaker);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            savings.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            studentChecking.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            checking.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(checking);
        }
        LOGGER.info("[DEBIT ACCOUNT ACCEPTED BEFORE SAVING (admin)] - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        Transaction doneTransaction = transactionRepository.save(transaction);
        LOGGER.info("[DEBIT ACCOUNT ACCEPTED AND SAVED (admin)] - AdminId: " + securedUser.getId() + " - MakerAccountId: " + accountId + " - Amount:" + amountDTO.getAmount());
        TransactionComplete response = new TransactionComplete();
        response.setAmount(amountDTO.getAmount());
        response.setTransactionMakerId(transactionMaker.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getDebitedAccount().getBalance()));
        return response;
    }

    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete debitAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO operationDTO) {
        LOGGER.info("[DEBIT ACCOUNT INIT(third-party)] - hashedKey: " + hashedKey + " - AccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
        User thirdParty = thirdPartyRepository.findByHashedKey(hashedKey).orElseThrow(() -> new NoSuchThirdPartyException("There's no third-party with provided credentials"));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: two transfers in 1 second period - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for account reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for account reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfClient);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(thirdParty.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(thirdParty.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
                LOGGER.info("[DEBIT ACCOUNT POSSIBLE FRAUD DETECTED(third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount() + " - Count:" + currentDayTransactionOfUser);
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Transaction transaction = new Transaction(new Money(operationDTO.getAmount()), new Date(), thirdParty);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.debitAccount(new Money(operationDTO.getAmount()));
            transaction.setDebitedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            if (!savings.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            savings.debitAccount(new Money(operationDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setDebitedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            if (!studentChecking.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            studentChecking.debitAccount(new Money(operationDTO.getAmount()));
            transaction.setDebitedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            if (!checking.getSecretKey().equals(operationDTO.getAccountSecretKey())) throw new NoPermissionForUserException("Invalid account secret key");
            checking.debitAccount(new Money(operationDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setDebitedAccount(checking);
        }
        LOGGER.info("[DEBIT ACCOUNT ACCEPTED BEFORE SAVE (third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
        Transaction doneTransaction = transactionRepository.save(transaction);
        LOGGER.info("[DEBIT ACCOUNT ACCEPTED AND SAVED (third-party)] Message: max daily amount for third-party reached - Third-PartyId: " + thirdParty.getId() + " - MakerAccountId: " + accountId + " - Amount:" + operationDTO.getAmount());
        TransactionComplete response = new TransactionComplete();
        response.setAmount(operationDTO.getAmount());
        response.setTransactionMakerId(thirdParty.getId());
        return response;
    }
}

/*
    // change the return to make a balanceViewModel
    public Object findById(Long id) {
        Optional<CreditCard> creditCard = creditCardRepository.findById(id);
        if (creditCard.isPresent()) return creditCard.get();
        Optional<Savings> savings = savingsRepository.findById(id);
        if (savings.isPresent()) return savings.get();
        Optional<StudentChecking> studentChecking = studentCheckingRepository.findById(id);
        if (studentChecking.isPresent()) return studentChecking.get();
        Optional<Checking> checking =  checkingRepository.findById(id);
        if (checking.isPresent()) return checking.get();
        throw new NoSuchAccountException("There's no account with provided ID");
    }

    @Secured({"ROLE_ADMIN", "ROLE_ACCOUNTHOLDER"})
    public AccountBalance getBalanceById(Long id, SecuredUser securedUser) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        System.out.println(account.getClass());
        for (Role role : securedUser.getRoles()) {
            if (role.getRole().equals("ROLE_ADMIN")) return new AccountBalance(account.getBalance());
        }
        if (account.hasAccess(securedUser.getId())) {
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }

        if (securedUser.getId().equals(account.getPrimaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else if (account.getSecondaryOwner() != null && securedUser.getId().equals(account.getSecondaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }


    }
    */

