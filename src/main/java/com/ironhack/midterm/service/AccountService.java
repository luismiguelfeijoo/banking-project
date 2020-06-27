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

    @Secured({"ROLE_ACCOUNTHOLDER"})
    public AccountBalance getBalanceById(Long accountId, SecuredUser securedUser) {
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
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }
    }

    @Secured({"ROLE_ADMIN"})
    public AccountBalance getBalanceById(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account instanceof CreditCard) {
            ((CreditCard) account).applyInterestRate();
        } else if (account instanceof Savings) {
            ((Savings) account).applyInterestRate();
            ((Savings) account).applyMaintenanceFee();
        } else if (account instanceof Checking) {
            ((Checking) account).applyMaintenanceFee();
        }
        return new AccountBalance(account.getBalance());
    }

    @Secured({"ROLE_ACCOUNTHOLDER"})
    public List<AccountBalance> getAllBalanceByUserId(SecuredUser securedUser) {
        List<Account> accounts = accountRepository.findByPrimaryOwnerId(securedUser.getId());
        //if (accounts.size() == 0) throw new NoSuchAccountException("User doesn't have registered accounts");
        return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
    }

    @Secured({"ROLE_ADMIN"})
    public List<AccountBalance> getAllBalanceByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByPrimaryOwnerId(userId);
        if (accounts.size() == 0) throw new NoSuchAccountException("User doesn't have registered accounts");
        return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
    }

    @Secured({"ROLE_ACCOUNTHOLDER"})
    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete transfer(Long accountId, SecuredUser securedUser, TransferDTO transferDTO) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (!account.hasAccess(securedUser.getId())) throw new NoPermissionForUserException("You don't have permission to do this transfer");
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Account receiverAccount = accountRepository.findById(transferDTO.getReceiverAccountId()).orElseThrow(() -> new NoSuchAccountException("There's no reciever account with provided ID"));
        if (!receiverAccount.getPrimaryOwner().getName().equals(transferDTO.getReceiverName())) {
            if (receiverAccount.getSecondaryOwner() == null)  {
                throw new NoPermissionForUserException("The user doesn't correspond with the owner of the account");
            } else if (!receiverAccount.getSecondaryOwner().getName().equals(transferDTO.getReceiverName())){
                throw new NoPermissionForUserException("The user doesn't correspond with the owner of the account");
            }
        }
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        Transaction transaction = new Transaction(new Money(transferDTO.getAmount()), new Date(), transactionMaker);
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
        receiverAccount.creditAccount(new Money(transferDTO.getAmount()));
        transaction.setCreditedAccount(receiverAccount);
        Transaction doneTransaction = transactionRepository.save(transaction);
        TransactionComplete response = new TransactionComplete();
        response.setAmount(transferDTO.getAmount());
        response.setTransactionMakerId(transactionMaker.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getDebitedAccount().getBalance()));
        return response;
    }

    @Secured({"ROLE_ADMIN"})
    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete creditAccount(Long accountId, SecuredUser securedUser, AmountDTO amountDTO) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(securedUser.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(securedUser.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        System.out.println(maxTransactionOfClients);
        System.out.println(currentDayTransactionOfClient);
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
        Transaction doneTransaction = transactionRepository.save(transaction);
        TransactionComplete response = new TransactionComplete();
        response.setAmount(amountDTO.getAmount());
        response.setTransactionMakerId(transactionMaker.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getCreditedAccount().getBalance()));
        return response;
    }

    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete creditAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO operationDTO) {
        ThirdParty thirdParty = thirdPartyRepository.findByHashedKey(hashedKey).orElseThrow(() -> new NoSuchThirdPartyException("There's no third-party with provided credentials"));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(thirdParty.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(thirdParty.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
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
        Transaction doneTransaction = transactionRepository.save(transaction);
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
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(securedUser.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(securedUser.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
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
        Transaction doneTransaction = transactionRepository.save(transaction);
        TransactionComplete response = new TransactionComplete();
        response.setAmount(amountDTO.getAmount());
        response.setTransactionMakerId(transactionMaker.getId());
        response.setUserAccount(new AccountBalance(doneTransaction.getDebitedAccount().getBalance()));
        return response;
    }

    @Transactional(noRollbackFor = {FraudDetectionException.class})
    public TransactionComplete debitAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO operationDTO) {
        User thirdParty = thirdPartyRepository.findByHashedKey(hashedKey).orElseThrow(() -> new NoSuchThirdPartyException("There's no third-party with provided credentials"));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account.getStatus() == AccountStatus.FROZEN) throw new FraudDetectionException("Your account is blocked for fraud inspection purposes, please contact customer service");
        if (transactionRepository.findTransactionOneSecondAgo(accountId, new Date()).size() > 0) {
            account.setStatus(AccountStatus.FROZEN);
            accountRepository.save(account);
            throw new FraudDetectionException("Possible fraud detected!");
        }
        Double maxTransactionOfClients = transactionRepository.findHighestTotalTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        Double currentDayTransactionOfClient = transactionRepository.findCurrentDateTransactionCountOfOwner(account.getPrimaryOwner().getId(), new Date());
        if ( maxTransactionOfClients != null  ) {
            if (currentDayTransactionOfClient.compareTo(maxTransactionOfClients * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfClient != null && currentDayTransactionOfClient.compareTo(2.0) >= 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        }
        Double maxTransactionOfUser = transactionRepository.findHighestTotalTransactionCountOfUser(thirdParty.getId(), new Date());
        Double currentDayTransactionOfUser = transactionRepository.findCurrentDateTransactionCountOfUser(thirdParty.getId(), new Date());
        if ( maxTransactionOfUser != null ) {
            if (currentDayTransactionOfUser.compareTo(maxTransactionOfUser * 1.5) > 0) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                throw new FraudDetectionException("Possible fraud detected!");
            }
        } else {
            if (currentDayTransactionOfUser != null && currentDayTransactionOfUser.compareTo(2.0) >= 0) {
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
        Transaction doneTransaction = transactionRepository.save(transaction);
        TransactionComplete response = new TransactionComplete();
        response.setAmount(operationDTO.getAmount());
        response.setTransactionMakerId(thirdParty.getId());
        return response;
    }


}

