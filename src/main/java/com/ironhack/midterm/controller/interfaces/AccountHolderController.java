package com.ironhack.midterm.controller.interfaces;

import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.view_model.AccountBalance;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AccountHolderController {
    public List<AccountBalance> getBalance(SecuredUser securedUser, Long userId);
    public AccountBalance getBalance(SecuredUser securedUser, Long userId, Long accountId);
    public Transaction transferMoney(SecuredUser securedUser, Long userId,  Long accountId, TransferDTO transferDTO);
}
