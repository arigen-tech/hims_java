package com.hims.exception;

public class PrinterNotFoundException extends RuntimeException {
    public PrinterNotFoundException(String message) {
        super(message);
    }
}
