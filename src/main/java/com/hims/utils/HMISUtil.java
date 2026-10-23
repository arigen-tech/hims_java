package com.hims.utils;

import com.hims.exception.SDDException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class HMISUtil {

    private HMISUtil() {}

    public static String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        if (today.getMonthValue() < 4) {
            year--;
        }
        return String.format("%02d-%02d", year % 100, (year + 1) % 100);
    }

    public static String formatTransactionNumber(
            HMISTransaction transaction,
            String financialYear,
            Long sequence) {

        return transaction.getPrefix() + "/" + financialYear + "/" + sequence;
    }

    public static LocalTime getCurrentLocalTime(){
        return  LocalTime.now();
    }
    public  static LocalDateTime getCurrentLocalDateTime(){
        return  LocalDateTime.now();
    }

    public  static LocalDate getCurrentLocalDate(){
        return  LocalDate.now();
    }

    public static String getMaskedMobileNumber(String mobileNumber) {
        if (mobileNumber == null ) {
            throw  new SDDException("mobileNumber",
                    HttpStatus.BAD_REQUEST.value(),
                    "Mobile number cannot be null"
            );
        } else if ( mobileNumber.length() != 10) {
            throw  new SDDException("mobileNumber",
                    HttpStatus.BAD_REQUEST.value(),
                    "Mobile number must be 10 digits long"
            );

        }

        return mobileNumber.substring(0, 2)
                + "*****"
                + mobileNumber.substring(7);
    }
}
