package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DgMasInvestigationRequest {
    private String investigationName;
    private String confidential;
    private String investigationType;
    private String maxNormalValue;
    private String minNormalValue;
    private Long mainChargeCodeId;
    private Long uomId;
    private Long subChargeCodeId;
    private Long sampleId;
    private Long collectionId;
//    private String equipmentId;
//    private Long testOrderNo;
//    private String numericOrString;
//    private String hicCode;
//    private String quantity;
//    private String normalValue;
//    private String appointmentRequired;
//    private String multipleResults;
//    private String appearInDischargeSummary;
//    private String bloodReactionTest;
//    private String bloodBankScreenTest;
//    private String instructions;
//    private String discountApplicable;
//    private String genderApplicable;
//    private String discount;
//    private Double price;
    private List<DgSubMasInvestigationRequest> subMasInvestigationRequestlist;
    private List<DgFixedValueRequest> fixedValueRequestList;
    private List<DgNormalValueRequest> normalValueRequestList;
}
