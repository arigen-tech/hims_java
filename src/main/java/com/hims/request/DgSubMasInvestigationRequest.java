package com.hims.request;

import com.hims.entity.*;
import lombok.Data;

@Data
public class DgSubMasInvestigationRequest {
    private String subInvestigationCode;
    private String subInvestigationName;
    private Long orderNo;
    private String resultType;
    private String comparisonType;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private Long sampleId;
    private Long uomId;
    private Long investigationId;
}
