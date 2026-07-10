package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AppointmentBlock {
    private Long billingHdId;
    private Long visitId;
    private String visitType;
    private Long tokenNo;
    private String department;
    private String consultedDoctor;
    private String sessionName;
    private Instant visitDate;
    private Long billingPolicyId;
    private BigDecimal tariff;
    private BigDecimal discount;
    private BigDecimal amountAfterDiscount;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal totalAmount;
    private BigDecimal registrationCost;

    private String policyCode;
    private String policyType;
    private Integer policyEligibilityDays;
    private BigDecimal policyDiscountPercent;
    private String policyDescription;
}

