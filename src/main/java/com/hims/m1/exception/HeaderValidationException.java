package com.hims.m1.exception;

public class HeaderValidationException extends RuntimeException {

    private final String errorCode;

    public HeaderValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
