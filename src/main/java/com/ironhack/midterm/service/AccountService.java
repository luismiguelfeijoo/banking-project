package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.NoPermissionForUserException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.repository.*;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.util.Optional;

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


}

