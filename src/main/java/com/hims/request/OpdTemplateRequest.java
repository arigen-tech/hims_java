package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class OpdTemplateRequest {
    private String opdTemplateCode;
    private String opdTemplateName;
    private String opdTemplateType;
    private Long departmentId;
    List<OpdTemplateInvestigationRequest> investigationRequestList;
}
