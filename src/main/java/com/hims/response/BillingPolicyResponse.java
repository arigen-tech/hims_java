package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class BillingPolicyResponse {
    private Long billingPolicyId;
    private String policyCode;
    private String description;
    private String applicableBillingType;
    private Integer followupDaysAllowed;
    private Integer discountPercentage;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdateDate;
}
