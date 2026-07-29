package com.hims.utils;

public enum HMISTransaction {
//
    
    ADMISSION_NO("ADMISSION_NO", "ADM"),
    DISCHARGE_NO("DISCHARGE_NO", "DIS"),
    LAB_NO("LAB_NO", "LAB"),
    RADIOLOGY_NO("RADIOLOGY_NO", "RAD"),
    BILL_NO("BILL_NO", "BILL"),
    RECEIPT_NO("RECEIPT_NO", "RCPT"),
    APPOINTMENT_NO("APPOINTMENT_NO", "APT"),
    OT_NO("OT_NO", "OT"),
    REFUND_NO("REFUND_NO", "REF");

    private final String transactionName;
    private final String prefix;

    HMISTransaction(String transactionName, String prefix) {
        this.transactionName = transactionName;
        this.prefix = prefix;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public String getPrefix() {
        return prefix;
    }
}
