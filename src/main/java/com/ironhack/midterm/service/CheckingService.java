package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.exceptions.NoSuchCheckingAccountException;
import com.ironhack.midterm.exceptions.NoSuchStudentCheckingAccountException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.StudentChecking;
import com.ironhack.midterm.repository.CheckingRepository;
import com.ironhack.midterm.utils.DateDifference;
import com.ironhack.midterm.utils.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CheckingService {
    @Autowired
    private CheckingRepository checkingRepository;
    @Autowired
    private AccountHolderService accountHolderService;
    @Autowired
    private StudentCheckingService studentCheckingService;

    private final static Logger LOGGER = LogManager.getLogger(CheckingService.class);


    // ¿Maybe transactional?
    @Secured({"ROLE_ADMIN"})
    public Checking create(AccountDTO accountDTO) {
        LOGGER.info("[CREATE CHECKING ACCOUNT (admin)]");
        AccountHolder primaryOwner = null;
        AccountHolder secondaryOwner = null;
        Checking checking = null;
        if (accountDTO.getPrimaryOwner().getId() != null) {
            primaryOwner = accountHolderService.findById(accountDTO.getPrimaryOwner().getId());
            if (DateDifference.yearDifference(primaryOwner.getDateOfBirth()) < 24) return studentCheckingService.create(accountDTO);
            checking = new Checking(new Money(accountDTO.getBalance()), primaryOwner);
        } else {
            // create new accountHolder
            if (DateDifference.yearDifference(accountDTO.getPrimaryOwner().getDateOfBirth()) < 24) return studentCheckingService.create(accountDTO);
            /*
            primaryOwner = new AccountHolder(accountDTO.getPrimaryOwner().getUsername(), accountDTO.getPrimaryOwner().getName(), accountDTO.getPrimaryOwner().getPassword(), accountDTO.getPrimaryOwner().getDateOfBirth(), accountDTO.getPrimaryOwner().getPrimaryAddress());
            if (accountDTO.getPrimaryOwner().getMailingAddress() != null) primaryOwner.setMailingAddress(accountDTO.getPrimaryOwner().getMailingAddress());
            primaryOwner = accountHolderRepository.save(primaryOwner);
             */
            primaryOwner = accountHolderService.create(accountDTO.getPrimaryOwner());
            checking = new Checking(new Money(accountDTO.getBalance()), primaryOwner);
        }
        if (accountDTO.getSecondaryOwner() != null) {
            if (accountDTO.getSecondaryOwner().getId() != null) {
                secondaryOwner = accountHolderService.findById(accountDTO.getSecondaryOwner().getId());
                checking.setSecondaryOwner(secondaryOwner);
            } else {
                /*
                secondaryOwner = new AccountHolder(accountDTO.getSecondaryOwner().getUsername(), accountDTO.getSecondaryOwner().getName(), accountDTO.getSecondaryOwner().getPassword(), accountDTO.getSecondaryOwner().getDateOfBirth(), accountDTO.getSecondaryOwner().getPrimaryAddress());
                if (accountDTO.getSecondaryOwner().getMailingAddress() != null) secondaryOwner.setMailingAddress(accountDTO.getSecondaryOwner().getMailingAddress());
                secondaryOwner = accountHolderRepository.save(secondaryOwner);
                 */
                secondaryOwner = accountHolderService.create(accountDTO.getSecondaryOwner());
                checking.setSecondaryOwner(secondaryOwner);
            }
        }
        return checkingRepository.save(checking);
    }

    public Checking findById(Long id) {
        return checkingRepository.findById(id).orElseThrow(() -> new NoSuchCheckingAccountException("There's no checking account with the provided ID"));
    }
}
