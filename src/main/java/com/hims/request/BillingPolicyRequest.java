package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillingPolicyRequest {
    private String policyCode;
    private String description;
    private String applicableBillingType;
    private Integer followupDaysAllowed;
    private BigDecimal discountPercentage;
}
