package com.hims.request;

import com.hims.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class DgSubMasInvestigationRequest {
    private Long subInvestigationId;
    private String subInvestigationCode;
    private String subInvestigationName;
    private String resultType;
    private String comparisonType;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private Long uomId;
    private String fixedValueExpectedResult;

    private List<DgFixedValueRequest> fixedValues;
    private List<DgNormalValueRequest> normalValues;

    private List<Long> fixedValueIdsToDelete;
    private List<Long> normalValueIdsToDelete;
}
