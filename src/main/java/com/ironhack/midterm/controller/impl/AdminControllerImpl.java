package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.interfaces.AdminController;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminControllerImpl implements AdminController {

    @Override
    @PostMapping("/admin/accounts")
    // maybe route is better on /accounts
    public Account createAccount(@RequestBody AccountDTO account) {
        // receive dto and create proper account
        return null;
    }

    @Override
    public AccountHolder createAccountHolder() {
        return null;
    }

    @Override
    public void addThirdParty() {

    }

    @Override
    public Account accessAccount() {
        return null;
    }

    @Override
    public void debitAccount() {

    }

    @Override
    public void creditAccount() {

    }
}
