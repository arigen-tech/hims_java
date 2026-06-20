package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyIndexOTPRequest {



    @NotBlank(message = "TxnId ID by cannot be null or empty.")
    String txnId;

    @NotBlank(message = "Otp type by cannot be null or empty.")
    private String inputType;

    @NotBlank(message = "Index by cannot be null or empty.")
    private String index;


    @NotBlank(message = "IsType cannot be null or empty.")
    String isType;

}
