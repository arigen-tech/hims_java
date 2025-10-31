package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class DgMasInvestigationMultiRequest {
    private Long investigationId;
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
    private String genderApplicable;
    private List<DgSubMasInvestigationRequest> masInvestReq;
    private List<Long> subInvestigationIdsToDelete;
}
