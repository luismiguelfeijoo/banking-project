package com.ironhack.midterm.handler;

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
}
