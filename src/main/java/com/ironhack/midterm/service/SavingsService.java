package com.ironhack.midterm.service;

import com.ironhack.midterm.model.Savings;
import com.ironhack.midterm.repository.SavingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class SavingsService {
    @Autowired
    private SavingsRepository savingsRepository;

    @Secured({"ROLE_ADMIN"})
    public Savings create(Savings savingsAccount) {
        return savingsRepository.save(savingsAccount);
    }
}
