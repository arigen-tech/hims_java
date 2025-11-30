package com.hims.response;

import lombok.Data;

@Data
public class OpdTemplateInvestigationResponse {
    private Long templateInvestigationId;
    private String investigationName;
    private Long opdTemplateId;
    private Long investigationId;
}
