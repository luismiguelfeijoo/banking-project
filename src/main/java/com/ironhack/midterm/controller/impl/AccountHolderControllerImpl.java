package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.controller.interfaces.AccountHolderController;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.service.AccountHolderService;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountHolderControllerImpl implements AccountHolderController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountHolderService accountHolderService;

    @Override
    //@GetMapping("/account-holders/{id}/accounts") , @PathVariable(name = "id") Long userId
    @GetMapping("/accounts")
    public List<AccountBalance> getAllBalance(@AuthenticationPrincipal SecuredUser securedUser) {
        return accountService.getAllBalanceByUserId(securedUser);
    }

    @Override
    //@GetMapping("/account-holders/{user-id}/accounts/{account-id}") , @PathVariable(name = "user-id") Long userId
    @GetMapping("/accounts/{account-id}")
    public AccountBalance getBalance(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "account-id") Long accountId) {
        return accountService.getBalanceById(accountId, securedUser);
    }

    @Override
    //@PostMapping("/account-holders/{user-id}/accounts/{account-id}/transfer") , @PathVariable(name = "user-id") Long userId
    @PostMapping("/accounts/{account-id}/transfer")
    public TransactionComplete transferMoney(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "account-id") Long accountId, @RequestBody TransferDTO transferDTO) {
        return accountService.transfer(accountId, securedUser, transferDTO);
    }

    @GetMapping("/login")
    public void login(@AuthenticationPrincipal SecuredUser securedUser) {
        accountHolderService.login(securedUser);
    }

    @GetMapping("/logout")
    public void logout(@AuthenticationPrincipal SecuredUser securedUser) {
        accountHolderService.logout(securedUser);
    }
}
