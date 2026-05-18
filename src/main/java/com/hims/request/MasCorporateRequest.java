package com.hims.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasCorporateRequest {

    @NotBlank(message = "Corporate name is required")
    private String corporateName;

    @NotBlank(message = "Corporate code is required")
    private String corporateCode;

    private String contactPerson;

    private String contactNo;

    @Email(message = "Invalid email format")
    private String emailId;

    private String address;

    private String creditAllowed;

    private Integer creditDays;
}