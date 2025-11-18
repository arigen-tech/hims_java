package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PendingBillingResponse {

    private Long billinghdid;
    private Long patientid;
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
    private BigDecimal registrationCost;
    private Integer  orderhdid;
    private String orderhdPaymentStatus;
    private String flag;
    private String source;



    private List<BillingDetailResponse> details;

}
