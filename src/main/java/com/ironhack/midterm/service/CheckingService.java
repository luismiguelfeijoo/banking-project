package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.CheckingRepository;
import com.ironhack.midterm.repository.StudentCheckingRepository;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CheckingService {
    @Autowired
    private CheckingRepository checkingRepository;
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private StudentCheckingService studentCheckingService;

    // ¿Maybe transactional?
    @Secured({"ROLE_ADMIN"})
    public Checking create(AccountDTO accountDTO) {
        AccountHolder primaryOwner = null;
        AccountHolder secondaryOwner = null;
        Checking checking = null;
        if (accountDTO.getPrimaryOwner().getId() != null) {
            primaryOwner = accountHolderRepository.findById(accountDTO.getPrimaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
            if (DateDifference.yearDifference(primaryOwner.getDateOfBirth()) < 24) return studentCheckingService.create(accountDTO);
            checking = new Checking(new Money(accountDTO.getBalance()), primaryOwner);
        } else {
            // create new accountHolder
            if (DateDifference.yearDifference(accountDTO.getPrimaryOwner().getDateOfBirth()) < 24) return studentCheckingService.create(accountDTO);
            primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), accountDTO.getPrimaryOwner().getName(), accountDTO.getPrimaryOwner().getPassword(), accountDTO.getPrimaryOwner().getDateOfBirth(), accountDTO.getPrimaryOwner().getPrimaryAddress());
            if (accountDTO.getPrimaryOwner().getMailingAddress() != null) primaryOwner.setMailingAddress(accountDTO.getPrimaryOwner().getMailingAddress());
            primaryOwner = accountHolderRepository.save(primaryOwner);
            checking = new Checking(new Money(accountDTO.getBalance()), primaryOwner);
        }
        if (accountDTO.getSecondaryOwner() != null) {
            if (accountDTO.getSecondaryOwner().getId() != null) {
                secondaryOwner = accountHolderRepository.findById(accountDTO.getSecondaryOwner().getId()).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
                checking.setSecondaryOwner(secondaryOwner);
            } else {
                secondaryOwner = new AccountHolder(accountDTO.getSecondaryOwner().getUsername(), accountDTO.getSecondaryOwner().getName(), accountDTO.getSecondaryOwner().getPassword(), accountDTO.getSecondaryOwner().getDateOfBirth(), accountDTO.getSecondaryOwner().getPrimaryAddress());
                if (accountDTO.getSecondaryOwner().getMailingAddress() != null) secondaryOwner.setMailingAddress(accountDTO.getSecondaryOwner().getMailingAddress());
                secondaryOwner = accountHolderRepository.save(secondaryOwner);
                checking.setSecondaryOwner(secondaryOwner);
            }
        }
        return checkingRepository.save(checking);
    }
}
