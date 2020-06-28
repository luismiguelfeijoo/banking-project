package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AccountHolderController {
    public List<AccountBalance> getAllBalance(SecuredUser securedUser);
    public AccountBalance getBalance(SecuredUser securedUser, Long accountId);
    public TransactionComplete transferMoney(SecuredUser securedUser, Long accountId, TransferDTO transferDTO);
}
