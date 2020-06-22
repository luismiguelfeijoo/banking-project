package com.ironhack.midterm.service;

import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.StudentCheckingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class StudentCheckingService {
    @Autowired
    private StudentCheckingRepository studentCheckingRepository;

    @Secured({"ROLE_ADMIN"})
    public StudentChecking create(StudentChecking studentCheckingAccount) {
        return studentCheckingRepository.save(studentCheckingAccount);
    }
}
