package com.ironhack.midterm.service;

import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.repository.CheckingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CheckingService {
    @Autowired
    private CheckingRepository checkingRepository;

    @Secured({"ROLE_ADMIN"})
    public Checking create(Checking checkingAccount) {
        return checkingRepository.save(checkingAccount);
    }
}
