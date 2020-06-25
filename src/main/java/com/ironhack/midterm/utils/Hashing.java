package com.ironhack.midterm.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Hashing {

    public static void main(String[] args) {
        BigDecimal interestRate = new BigDecimal("0.2");
        System.out.println(interestRate.divide(new BigDecimal("12"), 3, RoundingMode.HALF_EVEN));
    }

    public static String hash(String key) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(key);
    }

}
