package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DgMasInvestigationSingleReqest {
    private String investigationName;
    private String confidential;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private Long sampleId;
    private Long collectionId;
    private Long uomId;
    private String maxNormalValue;
    private String minNormalValue;
    private String investigationType;
    private String genderApplicable;
    private Long categoryId;
    private Long methodId;
    private String interpretation;
}
