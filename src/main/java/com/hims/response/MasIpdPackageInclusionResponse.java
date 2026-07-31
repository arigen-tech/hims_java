package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MasIpdPackageInclusionResponse {
    private Long inclusionId;
    private Long packageId;
    private Long serviceCategoryId;
    private  String serviceCategoryName;
    private String includedFlag;
    private BigDecimal limitAmount;
    private Integer limitQty;
}
