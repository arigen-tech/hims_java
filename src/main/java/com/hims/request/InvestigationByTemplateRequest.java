package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InvestigationByTemplateRequest {
    Long templateId;
    List<InvestigationForTemplateRequest> investigationForTemplateRequestList;
    List<Long> investigationIdsToDelete;
}
