package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class PendingBillingSearchResponse {

    private Long billinghdid;
    private String patientName;
    private String address;
    private Long patientid;
    private String uhidNo;
    private String mobileNo;
    private String age;
    private String sex;
    private String relation;
    private String consultedDoctor;
    private String department;
    private String billingType;
    private BigDecimal amount;
    private String billingStatus;
    private List<BillingDetailResponse> details;

}
