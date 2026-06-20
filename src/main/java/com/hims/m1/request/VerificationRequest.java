package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificationRequest {



    @NotNull(message = "Input type by cannot be null or empty.")
    String inputType;

    @NotNull(message = "INPUT number by cannot be null or empty.")
    String inputNumber;

    String authMethod;

    String key;

}
