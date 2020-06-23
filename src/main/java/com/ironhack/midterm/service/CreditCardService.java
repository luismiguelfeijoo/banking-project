package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.model.CreditCard;
import com.ironhack.midterm.repository.CreditCardRepository;
import com.ironhack.midterm.utils.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CreditCardService {
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Secured({"ROLE_ADMIN"})
    public CreditCard create(AccountDTO accountDTO) {
        CreditCard creditCard = new CreditCard(new Money(accountDTO.getBalance()), accountDTO.getPrimaryOwner());
        if (accountDTO.getSecondaryOwner() != null) creditCard.setSecondaryOwner(accountDTO.getSecondaryOwner());
        if (accountDTO.getInterestRate() != null) creditCard.setInterestRate(accountDTO.getInterestRate());
        if (accountDTO.getCreditLimit() != null) creditCard.setCreditLimit(accountDTO.getCreditLimit());
        return creditCardRepository.save(creditCard);
    }
}
