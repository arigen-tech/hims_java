package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OpdTemplateResponse {
    private Long templateId;
    private String opdTemplateCode;
    private String opdTemplateName;
    private String opdTemplateType;
    private String lastChgBy;
    private Instant lastChgDate;
    private String status;
    private Long departmentId;
    private Long doctorId;
    List<OpdTemplateInvestigationResponse> investigationResponseList;
}
