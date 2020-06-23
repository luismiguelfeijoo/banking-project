package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.controller.interfaces.AdminController;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.ThirdParty;
import com.ironhack.midterm.service.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class AdminControllerImpl implements AdminController {
    @Autowired
    private ThirdPartyService thirdPartyService;

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
    @PostMapping("/admin/third-party")
    public ThirdParty addThirdParty(@Valid @RequestBody ThirdPartyDTO thirdPartyDTO) {
        return thirdPartyService.create(thirdPartyDTO);
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
