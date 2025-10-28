package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PendingBillingResponse {

    private Long billinghdid;
    private String patientid;
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
    private String address;


    private List<BillingDetailResponse> details;

}
