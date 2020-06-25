package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.controller.interfaces.AccountController;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @PostMapping("/accounts/{id}/transfer")
    public void transferMoney(@Valid @RequestBody TransferDTO transferDTO) {

    }

    @Override
    public void creditAccount() {

    }

    @Override
    public void debitAccount() {

    }
}
