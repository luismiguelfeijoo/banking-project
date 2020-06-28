package com.ironhack.midterm.handler;

import com.ironhack.midterm.exceptions.*;
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

    // not currently used
    @ExceptionHandler(NegativeAmountException.class)
    public void negativeAmountExceptionHandler(NegativeAmountException negativeAmountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, negativeAmountException.getMessage());
    }

    @ExceptionHandler(NoSuchAccountException.class)
    public void noSuchAccountExceptionHandler(NoSuchAccountException noSuchAccountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchAccountException.getMessage());
    }

    @ExceptionHandler(NoSuchCheckingAccountException.class)
    public void noSuchCheckingAccountExceptionHandler(NoSuchCheckingAccountException noSuchCheckingAccountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchCheckingAccountException.getMessage());
    }

    @ExceptionHandler(NoSuchCreditCardException.class)
    public void noSuchCreditCardExceptionHandler(NoSuchCreditCardException noSuchCreditCardException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchCreditCardException.getMessage());
    }

    @ExceptionHandler(NoSuchSavingsAccountException.class)
    public void noSuchSavingsAccountExceptionHandler(NoSuchSavingsAccountException noSuchSavingsAccountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchSavingsAccountException.getMessage());
    }

    @ExceptionHandler(NoSuchStudentCheckingAccountException.class)
    public void noSuchStudentCheckingAccountExceptionHandler(NoSuchStudentCheckingAccountException noSuchStudentCheckingAccountException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchStudentCheckingAccountException.getMessage());
    }

    @ExceptionHandler(NoPermissionForUserException.class)
    public void noPermissionForUserExceptionHandler(NoPermissionForUserException noPermissionForUserException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, noPermissionForUserException.getMessage());
    }

    @ExceptionHandler(NoSuchUserException.class)
    public void noSuchUserExceptionHadler(NoSuchUserException noSuchUserException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, noSuchUserException.getMessage());
    }

    @ExceptionHandler(NoSuchThirdPartyException.class)
    public void noSuchThirdPartyExceptionHandler(NoSuchThirdPartyException noSuchThirdPartyException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, noSuchThirdPartyException.getMessage());
    }
    @ExceptionHandler(FraudDetectionException.class)
    public void fraudDetectionExceptionHanlder(FraudDetectionException fraudDetectionException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, fraudDetectionException.getMessage());
    }

    @ExceptionHandler(DuplicatedUsernameException.class)
    public void duplicateUsernameExceptionHandler(DuplicatedUsernameException duplicatedUsernameException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, duplicatedUsernameException.getMessage());
    }

    @ExceptionHandler(UserNotLoggedInException.class)
    public void userNotLoggedIdHanlder(UserNotLoggedInException userNotLoggedInException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NO_CONTENT, userNotLoggedInException.getMessage());
    }

    @ExceptionHandler(UserAlreadyLoggedInException.class)
    public void userNotLoggedIdHanlder(UserAlreadyLoggedInException userAlreadyLoggedInException, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NO_CONTENT, userAlreadyLoggedInException.getMessage());
    }
}
