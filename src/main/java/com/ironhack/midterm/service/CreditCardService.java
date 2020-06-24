package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.CreditCard;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.CreditCardRepository;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CreditCardService {
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private AccountHolderRepository accountHolderRepository;

    @Secured({"ROLE_ADMIN"})
    public CreditCard create(AccountDTO accountDTO) {
        // check for possible validation on fields
        AccountHolder primaryOwner = null;
        AccountHolder secondaryOwner = null;
        CreditCard creditCard = null;
        if (accountDTO.getPrimaryOwner().getId() != null) {
            primaryOwner = accountHolderRepository.findById(accountDTO.getPrimaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
            creditCard = new CreditCard(new Money(accountDTO.getBalance()), primaryOwner);
        } else {
            // create new accountHolder
            primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), accountDTO.getPrimaryOwner().getName(), accountDTO.getPrimaryOwner().getPassword(), accountDTO.getPrimaryOwner().getDateOfBirth(), accountDTO.getPrimaryOwner().getPrimaryAddress());
            if (accountDTO.getPrimaryOwner().getMailingAddress() != null) primaryOwner.setMailingAddress(accountDTO.getPrimaryOwner().getMailingAddress());
            primaryOwner = accountHolderRepository.save(primaryOwner);
            creditCard = new CreditCard(new Money(accountDTO.getBalance()), primaryOwner);
        }
        if (accountDTO.getSecondaryOwner() != null) {
            if (accountDTO.getSecondaryOwner().getId() != null) {
                secondaryOwner = accountHolderRepository.findById(accountDTO.getSecondaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
                creditCard.setSecondaryOwner(secondaryOwner);
            } else {
                secondaryOwner = new AccountHolder(accountDTO.getSecondaryOwner().getUsername(), accountDTO.getSecondaryOwner().getName(), accountDTO.getSecondaryOwner().getPassword(), accountDTO.getSecondaryOwner().getDateOfBirth(), accountDTO.getSecondaryOwner().getPrimaryAddress());
                if (accountDTO.getSecondaryOwner().getMailingAddress() != null) secondaryOwner.setMailingAddress(accountDTO.getSecondaryOwner().getMailingAddress());
                secondaryOwner = accountHolderRepository.save(secondaryOwner);
                creditCard.setSecondaryOwner(secondaryOwner);
            }
        }
        if (accountDTO.getInterestRate() != null) creditCard.setInterestRate(accountDTO.getInterestRate());
        if (accountDTO.getCreditLimit() != null) creditCard.setCreditLimit(accountDTO.getCreditLimit());
        return creditCardRepository.save(creditCard);
    }
}
