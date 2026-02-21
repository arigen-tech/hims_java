package com.hims.exception.patientRegistrationException;

public class TokenAlreadyBookedException extends RuntimeException{
    public TokenAlreadyBookedException(String message){
        super(message);
    }
}
