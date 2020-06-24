package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Savings;
import com.ironhack.midterm.repository.SavingsRepository;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class SavingsService {
    @Autowired
    private SavingsRepository savingsRepository;
    @Autowired
    private AccountHolderService accountHolderService;

    @Secured({"ROLE_ADMIN"})
    public Savings create(AccountDTO accountDTO) {
        AccountHolder primaryOwner = null;
        AccountHolder secondaryOwner = null;
        Savings savings = null;
        if (accountDTO.getPrimaryOwner().getId() != null) {
            primaryOwner = accountHolderService.findById(accountDTO.getPrimaryOwner().getId());
            savings = new Savings(new Money(accountDTO.getBalance()), primaryOwner);
        } else {
            // create new accountHolder
            /*
            primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), accountDTO.getPrimaryOwner().getName(), accountDTO.getPrimaryOwner().getPassword(), accountDTO.getPrimaryOwner().getDateOfBirth(), accountDTO.getPrimaryOwner().getPrimaryAddress());
            if (accountDTO.getPrimaryOwner().getMailingAddress() != null) primaryOwner.setMailingAddress(accountDTO.getPrimaryOwner().getMailingAddress());
            primaryOwner = accountHolderRepository.save(primaryOwner);
             */
            primaryOwner = accountHolderService.create(accountDTO.getPrimaryOwner());
            savings = new Savings(new Money(accountDTO.getBalance()), primaryOwner);
        }
        if (accountDTO.getSecondaryOwner() != null) {
            if (accountDTO.getSecondaryOwner().getId() != null) {
                secondaryOwner = accountHolderService.findById(accountDTO.getSecondaryOwner().getId());
                savings.setSecondaryOwner(secondaryOwner);
            } else {
                /*
                secondaryOwner = new AccountHolder(accountDTO.getSecondaryOwner().getUsername(), accountDTO.getSecondaryOwner().getName(), accountDTO.getSecondaryOwner().getPassword(), accountDTO.getSecondaryOwner().getDateOfBirth(), accountDTO.getSecondaryOwner().getPrimaryAddress());
                if (accountDTO.getSecondaryOwner().getMailingAddress() != null) secondaryOwner.setMailingAddress(accountDTO.getSecondaryOwner().getMailingAddress());
                secondaryOwner = accountHolderRepository.save(secondaryOwner);
                 */
                secondaryOwner = accountHolderService.create(accountDTO.getSecondaryOwner());
                savings.setSecondaryOwner(secondaryOwner);
            }
        }
        if (accountDTO.getSecondaryOwner() != null) savings.setSecondaryOwner(accountDTO.getSecondaryOwner());
        if (accountDTO.getInterestRate() != null) savings.setInterestRate(accountDTO.getInterestRate());
        return savingsRepository.save(savings);
    }
}
