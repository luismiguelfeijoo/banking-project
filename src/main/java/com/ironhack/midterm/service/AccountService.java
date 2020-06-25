package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.exceptions.NoPermissionForUserException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.exceptions.NoSuchUserException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        //System.out.println(account instanceof CreditCard);
        //System.out.println(securedUser.getRoles());
        for (Role role : securedUser.getRoles()) {
            if (role.getRole().equals("ROLE_ADMIN")) return new AccountBalance(account.getBalance());
        }
        if (securedUser.getId().equals(account.getPrimaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else if (account.getSecondaryOwner() != null && securedUser.getId().equals(account.getSecondaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }

    }

    @Secured({"ROLE_ADMIN", "ROLE_ACCOUNTHOLDER"})
    public AccountBalance getBalanceById(Long id, Long userId, SecuredUser securedUser) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (account instanceof CreditCard) {
            ((CreditCard) account).applyInterestRate();
        } else if (account instanceof Savings) {
            ((Savings) account).applyInterestRate();
        }
        for (Role role : securedUser.getRoles()) {
            if (role.getRole().equals("ROLE_ADMIN")) return new AccountBalance(account.getBalance());
        }
        if (securedUser.getId().equals(userId) && userId.equals(account.getPrimaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else if (account.getSecondaryOwner() != null && securedUser.getId().equals(account.getSecondaryOwner().getId()) && userId.equals(account.getSecondaryOwner().getId()) ) {
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }
        /*
        if (securedUser.getId().equals(account.getPrimaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else if (account.getSecondaryOwner() != null && securedUser.getId().equals(account.getSecondaryOwner().getId())) {
            return new AccountBalance(account.getBalance());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }

         */

    }


    public List<AccountBalance> getAllBalanceById(Long userId, SecuredUser securedUser) {
        List<Account> accounts = accountRepository.findByPrimaryOwnerId(userId);
        if (accounts.size() == 0) throw new NoSuchAccountException("User doesn't have registered accounts");
        for (Role role : securedUser.getRoles()) {
            if (role.getRole().equals("ROLE_ADMIN")) {
                return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
            }
        }
        if (securedUser.getId().equals(userId)) {
            return accounts.stream().map(account -> new AccountBalance(account.getBalance())).collect(Collectors.toList());
        } else {
            throw new NoPermissionForUserException("You don't have permission");
        }
    }

    @Secured({"ROLE_ACCOUNTHOLDER"})
    @Transactional
    public Transaction transfer(Long accountId, Long userId, SecuredUser securedUser, TransferDTO transferDTO) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        if (!account.hasAccess(userId) || !securedUser.getId().equals(userId)) throw new NoPermissionForUserException("You don't have permission to do this transfer");
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
            creditCard.creditAccount(new Money(transferDTO.getAmount()));
            transaction.setCreditedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            savings.creditAccount(new Money(transferDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setCreditedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            studentChecking.creditAccount(new Money(transferDTO.getAmount()));
            transaction.setCreditedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            checking.creditAccount(new Money(transferDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setCreditedAccount(checking);
        }
        receiverAccount.debitAccount(new Money(transferDTO.getAmount()));
        transaction.setDebitedAccount(receiverAccount);
        return transactionRepository.save(transaction);
    }

    @Secured({"ROLE_ADMIN"})
    @Transactional
    public Transaction creditAccount(Long accountId, SecuredUser securedUser, AmountDTO amountDTO) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
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
        return transactionRepository.save(transaction);
    }

    @Secured({"ROLE_ADMIN"})
    @Transactional
    public Transaction debitAccount(Long accountId, SecuredUser securedUser, AmountDTO amountDTO) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NoSuchAccountException("There's no account with provided ID"));
        User transactionMaker = userRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchUserException("There's no user with provided Id"));
        Transaction transaction = new Transaction(new Money(amountDTO.getAmount()), new Date(), transactionMaker);
        if (account instanceof CreditCard) {
            CreditCard creditCard = (CreditCard) account;
            creditCard.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(creditCard);
        } else if (account instanceof Savings) {
            Savings savings = (Savings) account;
            savings.debitAccount(new Money(amountDTO.getAmount()));
            savings.applyPenaltyFee();
            transaction.setDebitedAccount(savings);
        } else if (account instanceof StudentChecking) {
            StudentChecking studentChecking = (StudentChecking) account;
            studentChecking.debitAccount(new Money(amountDTO.getAmount()));
            transaction.setDebitedAccount(studentChecking);
        } else if (account instanceof Checking) {
            Checking checking = (Checking) account;
            checking.debitAccount(new Money(amountDTO.getAmount()));
            checking.applyPenaltyFee();
            transaction.setDebitedAccount(checking);
        }
        return transactionRepository.save(transaction);
    }


}

