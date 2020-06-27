package com.ironhack.midterm.controller.impl;

import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.controller.interfaces.ThirdPartyController;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
public class ThirdPartyControllerImpl implements ThirdPartyController {
    @Autowired
    private AccountService accountService;

    @Override
    @PutMapping("/third-party/accounts/{account-id}/debit")
    public TransactionComplete debitAccount(@RequestHeader("Hashed-Key")  UUID hashedKey, @PathVariable(name = "account-id") Long accountId, @Valid @RequestBody ThirdPartyOperationDTO thirdPartyOperationDTO) {
        return accountService.debitAccount(hashedKey, accountId, thirdPartyOperationDTO);
    }

    @Override
    @PutMapping("/third-party/accounts/{account-id}/credit")
    public TransactionComplete creditAccount(@RequestHeader("Hashed-Key") UUID hashedKey, @PathVariable(name = "account-id") Long accountId, @Valid @RequestBody ThirdPartyOperationDTO thirdPartyOperationDTO) {
        return accountService.creditAccount(hashedKey, accountId, thirdPartyOperationDTO);
    }
}
