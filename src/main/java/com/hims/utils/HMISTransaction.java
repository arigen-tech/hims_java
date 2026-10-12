package com.hims.utils;

public enum HMISTransaction {

    
    ADMISSION_NO("ADMISSION_NO", "ADM"),
    DISCHARGE_NO("DISCHARGE_NO", "DIS"),
    LAB_NO("LAB_NO", "LAB"),
    RADIOLOGY_NO("RADIOLOGY_NO", "RAD"),
    BILL_NO("BILL_NO", "BILL"),
    RECEIPT_NO("RECEIPT_NO", "RCPT"),
    APPOINTMENT_NO("APPOINTMENT_NO", "APT"),
    OT_NO("OT_NO", "OT"),
    REFUND_NO("REFUND_NO", "REF"),
    PRESCRIPTION_NO("PRESCRIPTION_NO", "PRES"),
    SURGERY_NO("SURGERY_NO", "SUR"),
    NIS_NO("NIS_NO", "NIS"),
    PROCEDURE_NO("PROCEDURE_NO", "PROC"),
    OT_BOOKING_NO("BOOK_NO", "OT"),
    PAYMENT_REFERENCE_NO("PAYMENT_REFERENCE_NO", "PAY"),
    REFUND_REFERENCE_NO("REFUND_REFERENCE_NO", "REFUND"),
    BLOOD_REQUEST_NO("BLOOD_REQUEST_NO", "BRQ"),
    BLOOD_UNIT_WB_NO("BLOOD_UNIT_WB_NO", "WB"),
    BLOOD_UNIT_PRBC_NO("BLOOD_UNIT_PRBC_NO", "PRBC"),
    BLOOD_UNIT_PLT_NO("BLOOD_UNIT_PLT_NO", "PLT"),
    BLOOD_UNIT_CRYO_NO("BLOOD_UNIT_CRYO_NO", "CRYO"),
    BLOOD_UNIT_FFP_NO("BLOOD_UNIT_FFP_NO", "FFP"),
    BLOOD_UNIT_SDP_NO("BLOOD_UNIT_SDP_NO", "SDP");



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
