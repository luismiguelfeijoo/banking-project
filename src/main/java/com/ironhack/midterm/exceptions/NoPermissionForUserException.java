package com.ironhack.midterm.exceptions;

public class NoPermissionForUserException extends RuntimeException {
    public NoPermissionForUserException(String message) {
        super(message);
    }
}
