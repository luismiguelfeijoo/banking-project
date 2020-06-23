package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.CheckingRepository;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CheckingService {
    @Autowired
    private CheckingRepository checkingRepository;
    @Autowired
    private AccountHolderRepository accountHolderRepository;

    @Secured({"ROLE_ADMIN"})
    public Checking create(AccountDTO accountDTO) {
        AccountHolder primaryOwner = null;
        AccountHolder secondaryOwner = null;

        Checking checking = null;
        if (accountDTO.getPrimaryOwner().getId() != null) {
            primaryOwner = accountHolderRepository.findById(accountDTO.getPrimaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
            checking = new Checking(new Money(accountDTO.getBalance()), primaryOwner);
        } else {
            // create new accountHolder
            // primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), accountDTO.)
        }

        if (accountDTO.getSecondaryOwner().getId() != null) {
            secondaryOwner = accountHolderRepository.findById(accountDTO.getSecondaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
            checking.setSecondaryOwner(secondaryOwner);
        } else if (accountDTO.getSecondaryOwner() != null) {
            // create new accountHolder
            //primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), )
        }
        return checkingRepository.save(checking);
    }
}
