package com.ironhack.midterm.exceptions;

public class NoSuchCreditCardException extends RuntimeException {
    public NoSuchCreditCardException(String message) {
        super(message);
    }
}
