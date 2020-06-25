package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.interfaces.AccountController;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountControllerImpl implements AccountController {
    @Autowired
    private AccountService accountService;

    /*
    @Override
    @GetMapping("/accounts/{id}")
    public Object accessAccount(@PathVariable(name = "id") Long accountId) {
        return accountService.findById(accountId);
    }
     */

    @Override
    @GetMapping("/accounts/{id}/balance")
    public AccountBalance accessAccountBalance(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "id") Long accountId) {
        return accountService.getBalanceById(accountId, securedUser);
    }

    @Override
    public void transferMoney() {

    }

    @Override
    public void creditAccount() {

    }

    @Override
    public void debitAccount() {

    }
}
