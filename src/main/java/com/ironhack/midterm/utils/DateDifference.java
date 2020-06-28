package com.ironhack.midterm.utils;

import org.joda.time.Years;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class DateDifference {
    public static int yearDifference(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        Period period = Period.between(localDate, now);
        //System.out.println(period.getYears());
        return period.getYears();
    }

    public static int monthDifference(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        Period period = Period.between(localDate, now);
        return period.getMonths();
    }
}
