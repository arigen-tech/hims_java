package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasCorporateResponse {
    private Long corporateId;
    private String corporateName;
    private String corporateCode;
    private String contactPerson;
    private String contactNo;
    private String address;
    private String email;
    private String creditAllowed;
    private Integer creditDays;
    private LocalDateTime lastChgDate;
    private String status;

}