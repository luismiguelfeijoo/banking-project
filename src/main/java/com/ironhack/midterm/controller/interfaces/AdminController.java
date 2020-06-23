package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.ThirdParty;

public interface AdminController {
    public Account createAccount(AccountDTO account);
    public AccountHolder createAccountHolder();
    public ThirdParty addThirdParty(ThirdPartyDTO thirdPartyDTO);
    // Add a view model to show the balance
    public Account accessAccount();
    public void debitAccount();
    public void creditAccount();
}
