package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DgMasInvestigationSingleResponse {
    private Long investigationId;
    private String investigationName;
    private String status;
    private String investigationType;
    private String confidential;
    private String multipleResults;
    private Long mainChargeCodeId;
    private String mainChargeCodeName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private Long sampleId;
    private String sampleName;
    private Long collectionId;
    private String collectionName;
    private Long uomId;
    private String uomName;
    private String maxNormalValue;
    private String minNormalValue;
    private String genderApplicable;
}
