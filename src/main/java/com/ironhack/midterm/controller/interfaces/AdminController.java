package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.ThirdParty;

import java.util.List;

public interface AdminController {
    public Account createAccount(AccountDTO account);
    public AccountHolder createAccountHolder(AccountHolder accountHolder);
    public ThirdParty addThirdParty(ThirdPartyDTO thirdPartyDTO);
    public List<AccountHolder> getAccountHolders();
    public AccountHolder getAccountHolder(Long id);
    // Add a view model to show the balance
    public Account accessAccount();
    public void debitAccount();
    public void creditAccount();
}
