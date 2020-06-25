package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.controller.interfaces.AccountHolderController;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountHolderControllerImpl implements AccountHolderController {
    @Autowired
    private AccountService accountService;

    @Override
    @GetMapping("/account-holders/{id}/accounts")
    public List<AccountBalance> getBalance(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "id") Long userId) {
        return accountService.getAllBalanceById(userId, securedUser);
    }

    @Override
    @GetMapping("/account-holders/{user-id}/accounts/{account-id}")
    public AccountBalance getBalance(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "user-id") Long userId, @PathVariable(name = "account-id") Long accountId) {
        return accountService.getBalanceById(accountId, userId, securedUser);
    }

    @Override
    @PostMapping("/account-holders/{user-id}/accounts/{account-id}/transfer")
    public Transaction transferMoney(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "user-id") Long userId, @PathVariable(name = "account-id") Long accountId, @RequestBody TransferDTO transferDTO) {
        return accountService.transfer(accountId, userId, securedUser, transferDTO);
    }
}
