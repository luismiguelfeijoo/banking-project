package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;

public interface AdminController {
    public Account createAccount();
    public AccountHolder createAccountHolder();
    public void addThirdParty();
    // Add a view model to show the balance
    public Account accessAccount();
    public void debitAccount();
    public void creditAccount();
}
