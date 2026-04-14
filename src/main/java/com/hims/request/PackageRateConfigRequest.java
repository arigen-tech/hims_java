package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PackageRateConfigRequest {
    private Long packageId;
    private Long billingTypeId;
    private Long insuranceId;
    private Long tpaId;
    private Long corporateId;
    private Long roomCategoryId;
    private BigDecimal amount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String preAuthRequired;
    private BigDecimal copayPercent;
    private BigDecimal maxClaimAmount;


}