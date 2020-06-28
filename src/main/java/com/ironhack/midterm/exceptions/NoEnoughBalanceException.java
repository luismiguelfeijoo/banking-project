package com.ironhack.midterm.exceptions;

public class NoEnoughBalanceException extends RuntimeException {
    public NoEnoughBalanceException(String message) {
        super(message);
    }
}
