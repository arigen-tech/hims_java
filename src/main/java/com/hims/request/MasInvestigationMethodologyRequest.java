package com.hims.request;

import lombok.Data;

@Data
public class MasInvestigationMethodologyRequest {
    private String methodName;
    private String note;

    private Long investigationId;
}
