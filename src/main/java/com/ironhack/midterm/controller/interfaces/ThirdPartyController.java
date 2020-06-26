package com.ironhack.midterm.controller.interfaces;


import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.model.Transaction;

public interface ThirdPartyController {
    public Transaction debitAccount(String hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
    public Transaction creditAccount(String hashedKey, Long accountId, ThirdPartyOperationDTO thirdPartyOperationDTO);
}
