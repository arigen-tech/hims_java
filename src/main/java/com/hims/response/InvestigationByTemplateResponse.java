package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InvestigationByTemplateResponse {
    private Long templateId;
    private String templateName;
    List<InvestigationForTemplateResponse> investigationForTemplateResponseList;
    List<Long> investigationIdsToDelete;
}
