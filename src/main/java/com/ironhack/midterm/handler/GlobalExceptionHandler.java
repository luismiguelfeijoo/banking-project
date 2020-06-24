package com.ironhack.midterm.handler;

import com.ironhack.midterm.exceptions.NegativeAmountException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchAccountHolderException.class)
    public void noSuchAccountHolderExceptionHandler(NoSuchAccountHolderException noSuchAccountHolderException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchAccountHolderException.getMessage());
    }

    @ExceptionHandler(NoEnoughBalanceException.class)
    public void noEnoughBalanceExceptionHandler(NoEnoughBalanceException noEnoughBalanceException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, noEnoughBalanceException.getMessage());
    }

    @ExceptionHandler(NegativeAmountException.class)
    public void negativeAmountExceptionHandler(NegativeAmountException negativeAmountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, negativeAmountException.getMessage());
    }
}
