package com.hims.response;

import lombok.Data;

@Data
public class MasInvestigationMethodologyResponse {
    private Long methodId;
    private String methodName;
    private Long investigationId;
    private String note;

}
