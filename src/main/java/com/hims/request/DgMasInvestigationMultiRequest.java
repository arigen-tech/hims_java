package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class DgMasInvestigationMultiRequest {
    private Long investigationId;
    private List<DgSubMasInvestigationRequest> masInvestReq;
    private List<Long> subInvestigationIdsToDelete;
}
