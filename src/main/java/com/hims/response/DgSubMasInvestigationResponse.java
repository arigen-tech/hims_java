package com.hims.response;

import com.hims.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Data
public class DgSubMasInvestigationResponse {
    private Long subInvestigationId;
    private String subInvestigationCode;
    private String subInvestigationName;
    private String status;
    private Long orderNo;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private String resultType;
    private String comparisonType;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private Long sampleId;
    private Long uomId;
    private Long investigationId;
    private String fixedValueExpectedResult;
    private List<DgFixedValueResponse> fixedValueResponseList;
    private List<DgNormalValueResponse> normalValueResponseList;
}
