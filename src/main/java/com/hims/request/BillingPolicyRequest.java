package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingPolicyRequest {
    private String policyCode;
    private String description;
    private String applicableBillingType;
    private Integer followupDaysAllowed;
    private Integer discountPercentage;
}
