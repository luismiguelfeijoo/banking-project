package com.ironhack.midterm.service;

import com.ironhack.midterm.exceptions.UserAlreadyLoggedInException;
import com.ironhack.midterm.exceptions.DuplicatedUsernameException;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.exceptions.UserNotLoggedInException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.repository.AccountHolderRepository;
import com.ironhack.midterm.repository.RoleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        AccountHolder newAccountHolder = new AccountHolder(accountHolder.getUsername(), accountHolder.getName(), accountHolder.getPassword(), accountHolder.getDateOfBirth(), accountHolder.getPrimaryAddress());
        LOGGER.info("[CREATE ACCOUNT HOLDER (admin)]");
        Role role = new Role();
        if (accountHolder.getMailingAddress() != null)
            newAccountHolder.setMailingAddress(accountHolder.getMailingAddress());
        role.setRole("ROLE_ACCOUNTHOLDER");
        role.setUser(newAccountHolder);
        AccountHolder result = null;;
        try {
            result = accountHolderRepository.save(newAccountHolder);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedUsernameException("There's already an user with the provided username");
        }
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

    public void login(SecuredUser securedUser) {
        AccountHolder user = accountHolderRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchAccountHolderException("Wrong username/password combination"));
        if (user.isLoggedIn()) throw new UserAlreadyLoggedInException("The user is already Logged In");
        user.login();
        accountHolderRepository.save(user);
    }

    public void logout(SecuredUser securedUser) {
        AccountHolder user = accountHolderRepository.findById(securedUser.getId()).orElseThrow(() -> new NoSuchAccountHolderException("Wrong username/password combination"));
        if (!user.isLoggedIn()) throw new UserNotLoggedInException("The user is already Logged Out");
        user.logout();
        accountHolderRepository.save(user);
    }
}
