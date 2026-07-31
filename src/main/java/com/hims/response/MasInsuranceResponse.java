package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasInsuranceResponse {
    private Long insuranceId;
    private String insuranceName;
    private String insuranceCode;
    private String contactPerson;
    private String contactNo;
    private String email;
    private String address;

    private LocalDateTime lastChgDate;
    private String status;
}
