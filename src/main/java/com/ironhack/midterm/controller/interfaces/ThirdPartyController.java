package com.ironhack.midterm.controller.interfaces;


import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.view_model.TransactionComplete;

import java.util.UUID;

public interface ThirdPartyController {
    public TransactionComplete debitAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
    public TransactionComplete creditAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
}
