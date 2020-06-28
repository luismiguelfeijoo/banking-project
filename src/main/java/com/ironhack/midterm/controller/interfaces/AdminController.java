package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;

import java.util.List;

public interface AdminController {
    public Account createAccount(AccountDTO account);
    public AccountHolder createAccountHolder(AccountHolder accountHolder);
    public ThirdParty addThirdParty(ThirdPartyDTO thirdPartyDTO);
    public List<AccountHolder> getAccountHolders();
    public AccountHolder getAccountHolder(Long id);
    // Add a view model to show the balance
    public AccountBalance accessAccount(Long accountId);
    public TransactionComplete debitAccount(SecuredUser securedUser, Long accountId, AmountDTO amountDTO);
    public TransactionComplete creditAccount(SecuredUser securedUser, Long accountId, AmountDTO amountDTO);
}
