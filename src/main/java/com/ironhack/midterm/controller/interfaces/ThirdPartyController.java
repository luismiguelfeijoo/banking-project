package com.ironhack.midterm.controller.interfaces;


import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.model.Transaction;

import java.util.UUID;

public interface ThirdPartyController {
    public Transaction debitAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
    public Transaction creditAccount(UUID hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
}
