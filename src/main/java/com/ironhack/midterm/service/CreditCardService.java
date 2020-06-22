package com.ironhack.midterm.service;

import com.ironhack.midterm.model.CreditCard;
import com.ironhack.midterm.repository.CreditCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CreditCardService {
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Secured({"ROLE_ADMIN"})
    public CreditCard create(CreditCard creditCard) {
        return creditCardRepository.save(creditCard);
    }
}
