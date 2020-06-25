package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.view_model.AccountBalance;

public interface AccountController {
    // create a route with different Accounts and id
    //public Object accessAccount(Long accountId);
    public AccountBalance accessAccountBalance(SecuredUser securedUser, Long accountId);
    public void transferMoney(TransferDTO transferDTO);
    public void creditAccount();
    public void debitAccount();
}
