package com.hims.request;

import com.hims.entity.*;
import lombok.Data;

@Data
public class DgSubMasInvestigationRequest {
    private String subInvestigationCode;
    private String subInvestigationName;
    private String resultType;
    private String comparisonType;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private Long uomId;
    private Long investigationId;
    //    private Long sampleId;
    //    private Long orderNo;
}
