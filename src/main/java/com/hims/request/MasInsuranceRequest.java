package com.hims.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasInsuranceRequest {

    @NotBlank(message = "Insurance name is required")
    private String insuranceName;

    private String insuranceCode;

    private String contactPerson;

    private String contactNo;

    @Email(message = "Invalid email format")
    private String emailId;

    private String address;
}