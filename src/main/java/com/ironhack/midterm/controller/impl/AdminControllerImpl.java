package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.controller.interfaces.AdminController;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.service.*;
import com.ironhack.midterm.utils.DateDifference;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@RestController
public class AdminControllerImpl implements AdminController {
    @Autowired
    private ThirdPartyService thirdPartyService;
    @Autowired
    private CheckingService checkingService;
    @Autowired
    private StudentCheckingService studentCheckingService;
    @Autowired
    private SavingsService savingsService;
    @Autowired
    private CreditCardService creditCardService;

    @Override
    @PostMapping("/admin/accounts")
    // maybe route is better on /accounts
    public Account createAccount(@Valid @RequestBody AccountDTO account) {
        Account newAccount = null;
        // receive dto and create proper account
        switch (account.getAccountType()) {
            case CHECKING:
                /*
                if (DateDifference.yearDifference(account.getPrimaryOwner().getDateOfBirth()) > 24) {
                    newAccount = checkingService.create(account);
                } else {
                    newAccount = studentCheckingService.create(account);
                }

                 */
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
