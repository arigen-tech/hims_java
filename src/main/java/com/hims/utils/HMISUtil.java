package com.hims.utils;

import java.time.LocalDate;
//
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
}
