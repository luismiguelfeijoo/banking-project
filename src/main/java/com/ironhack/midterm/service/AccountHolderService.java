package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountHolderService {
    @Autowired
    private AccountHolderRepository accountHolderRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Secured({"ROLE_ADMIN"})
    @Transactional
    public AccountHolder create(AccountHolder accountHolder) {
        AccountHolder newAccountHolder = new AccountHolder(accountHolder.getUsername(), accountHolder.getName(), accountHolder.getPassword(), accountHolder.getDateOfBirth(), accountHolder.getPrimaryAddress());
        Role role = new Role();
        if (accountHolder.getMailingAddress() != null)
            newAccountHolder.setMailingAddress(accountHolder.getMailingAddress());
        role.setRole("ROLE_ACCOUNTHOLDER");
        role.setUser(newAccountHolder);
        AccountHolder result = accountHolderRepository.save(newAccountHolder);
        roleRepository.save(role);
        return result;
    }

    public AccountHolder findById(Long id) {
        return accountHolderRepository.findById(id).orElseThrow(() -> new NoSuchAccountHolderException("There's no account holder with provided id"));
    }
}
