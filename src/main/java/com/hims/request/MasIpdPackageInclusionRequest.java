package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MasIpdPackageInclusionRequest {
    Long serviceCategoryId;
    Integer days;
    BigDecimal limitAmount;
    private String includedFlag;

}
