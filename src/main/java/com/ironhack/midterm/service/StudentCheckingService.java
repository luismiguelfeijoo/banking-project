package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.StudentCheckingRepository;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class StudentCheckingService {
    @Autowired
    private StudentCheckingRepository studentCheckingRepository;

    @Secured({"ROLE_ADMIN"})
    public StudentChecking create(AccountDTO accountDTO) {
        StudentChecking studentChecking = new StudentChecking(new Money(accountDTO.getBalance()), accountDTO.getPrimaryOwner());
        if (studentChecking.getSecondaryOwner() != null) studentChecking.setSecondaryOwner(accountDTO.getSecondaryOwner());
        return studentCheckingRepository.save(studentChecking);
    }
}
