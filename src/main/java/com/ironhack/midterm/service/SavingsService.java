package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
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

    @Secured({"ROLE_ADMIN"})
    public Savings create(AccountDTO accountDTO) {
        Savings savings = new Savings(new Money(accountDTO.getBalance()), accountDTO.getPrimaryOwner());
        if (accountDTO.getSecondaryOwner() != null) savings.setSecondaryOwner(accountDTO.getSecondaryOwner());
        if (accountDTO.getInterestRate() != null) savings.setInterestRate(accountDTO.getInterestRate());
        return savingsRepository.save(savings);
    }
}
