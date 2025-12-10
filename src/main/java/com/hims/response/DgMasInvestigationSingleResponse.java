package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DgMasInvestigationSingleResponse {
    private Long investigationId;
    private String investigationName;
    private String status;
    private String lastChgBy;
    private String lastChgTime;
    private Instant lastChgDate;
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
    private Long categoryId;
    private String categoryName;
    private Long methodId;
    private String methodName;
    private String maxNormalValue;
    private String minNormalValue;
    private String genderApplicable;
    private String interpretation;
}
