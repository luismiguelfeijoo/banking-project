package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.controller.interfaces.AdminController;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.service.*;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class AdminControllerImpl implements AdminController {
    @Autowired
    private ThirdPartyService thirdPartyService;
    @Autowired
    private AccountHolderService accountHolderService;
    @Autowired
    private CheckingService checkingService;
    @Autowired
    private StudentCheckingService studentCheckingService;
    @Autowired
    private SavingsService savingsService;
    @Autowired
    private CreditCardService creditCardService;
    @Autowired
    private AccountService accountService;

    @Override
    @PostMapping("/admin/accounts")
    // maybe route is better on /accounts
    public Account createAccount(@Valid @RequestBody AccountDTO account) {
        Account newAccount = null;
        // receive dto and create proper account
        switch (account.getAccountType()) {
            case CHECKING:
                newAccount = checkingService.create(account);
                break;
            case SAVINGS:
                newAccount = savingsService.create(account);
                break;
            case CREDITCARD:
                newAccount = creditCardService.create(account);
                break;
        }
        return newAccount;
    }

    @Override
    @GetMapping("/admin/accounts/{account-id}")
    public AccountBalance accessAccount(@PathVariable(name = "account-id") Long accountId) {
        return accountService.getBalanceById(accountId);
    }

    @Override
    @PutMapping("/admin/accounts/{account-id}/debit")
    public TransactionComplete debitAccount(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "account-id") Long accountId, @Valid @RequestBody AmountDTO amountDTO) {
        return accountService.debitAccount(accountId, securedUser, amountDTO);
    }

    @Override
    @PutMapping("/admin/accounts/{account-id}/credit")
    public TransactionComplete creditAccount(@AuthenticationPrincipal SecuredUser securedUser, @PathVariable(name = "account-id") Long accountId, @Valid @RequestBody AmountDTO amountDTO) {
        return accountService.creditAccount(accountId, securedUser, amountDTO);
    }

    @GetMapping("/admin/account-holders")
    public List<AccountHolder> getAccountHolders() {
        return accountHolderService.findAll();
    }


    @GetMapping("/admin/account-holders/{id}")
    public AccountHolder getAccountHolder(@PathVariable(name = "id") Long id) {
        return accountHolderService.findById(id);
    }

    @Override
    @PostMapping("/admin/account-holders")
    public AccountHolder createAccountHolder(@Valid @RequestBody AccountHolder accountHolder) {
        return accountHolderService.create(accountHolder);
    }

    @Override
    @PostMapping("/admin/third-party")
    public ThirdParty addThirdParty(@Valid @RequestBody ThirdPartyDTO thirdPartyDTO) {
        return thirdPartyService.create(thirdPartyDTO);
    }
}
