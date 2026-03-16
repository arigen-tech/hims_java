package com.hims.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class LaboratoryInvestigationPaymentDetails {
    private Long visitId;
    private Long billinghdid;
    private Long patientid;
    private String patientName;
    private String mobileNo;
    private String age;
    private String sex;
    private String relation;
    private String billingType;
    private String department;
    private BigDecimal amount;
    private String billingStatus;
    private String address;
    private Integer  orderhdid;
    private String orderhdPaymentStatus;
    private String flag;
    private String source;
    private String patientUhid;

    private Long tokenNo;
    private Instant visitDate;
    private String sessionName;
    private BigDecimal registrationCost;

    private Long billingPolicyId;
    private List<BillingDetailResponse> details;
}
