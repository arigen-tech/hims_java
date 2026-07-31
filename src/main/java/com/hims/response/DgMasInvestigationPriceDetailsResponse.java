package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DgMasInvestigationPriceDetailsResponse {
    private Long investigationId;
    private String investigationName;
    private String status;
    private Long mainChargeCodeId;
    private String mainChargeCodeName;
    private String discountApplicable;
    private String genderApplicable;
    private String discount;
    private Double price;
    private String sampleName;
    private String container;
}
