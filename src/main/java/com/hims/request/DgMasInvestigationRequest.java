package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DgMasInvestigationRequest {
    private String investigationName;
    private String confidential;
    private String appearInDischargeSummary;
    private String investigationType;
    private String multipleResults;
    private String quantity;
    private String normalValue;
    private String appointmentRequired;
    private String maxNormalValue;
    private String minNormalValue;
    private Long testOrderNo;
    private String numericOrString;
    private String hicCode;
    private Long mainChargeCodeId;
    private Long uomId;
    private Long subChargeCodeId;
    private Long sampleId;
    private String equipmentId;
    private Long collectionId;
    private String bloodReactionTest;
    private String bloodBankScreenTest;
    private String instructions;
    private String discountApplicable;
    private String genderApplicable;
    private String discount;
    private Double price;
}
