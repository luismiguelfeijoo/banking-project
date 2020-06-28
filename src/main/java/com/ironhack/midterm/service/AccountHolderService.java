package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.RoleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountHolderService {
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private RoleRepository roleRepository;

    private final static Logger LOGGER = LogManager.getLogger(AccountHolderService.class);

    @Secured({"ROLE_ADMIN"})
    @Transactional
    public AccountHolder create(AccountHolder accountHolder) {
        //AccountHolder newAccountHolder = new AccountHolder(accountHolder.getUsername(), accountHolder.getName(), accountHolder.getPassword(), accountHolder.getDateOfBirth(), accountHolder.getPrimaryAddress());
        LOGGER.info("[CREATE ACCOUNT HOLDER (admin)]");
        Role role = new Role();
        if (accountHolder.getMailingAddress() != null)
            accountHolder.setMailingAddress(accountHolder.getMailingAddress());
        role.setRole("ROLE_ACCOUNTHOLDER");
        role.setUser(accountHolder);
        AccountHolder result = accountHolderRepository.save(accountHolder);
        roleRepository.save(role);
        return result;
    }

    public AccountHolder findById(Long id) {
        return accountHolderRepository.findById(id).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
    }

    @Secured({"ROLE_ADMIN"})
    public List<AccountHolder> findAll() {
        return accountHolderRepository.findAll();
    }
}
