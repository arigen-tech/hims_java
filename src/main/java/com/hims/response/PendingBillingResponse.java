package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PendingBillingResponse {

    private Long id;
    private String patientName;
    private String mobileNo;
    private String age;
    private String sex;
    private String relation;
    private String billingType;
    private String consultedDoctor;
    private String department;
    private BigDecimal amount;
    private String billingStatus;

}
