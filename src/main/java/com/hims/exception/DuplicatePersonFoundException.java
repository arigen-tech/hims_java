package com.hims.exception;

public class DuplicatePersonFoundException extends RuntimeException {
    public DuplicatePersonFoundException(String message) {
        super(message);
    }
}
