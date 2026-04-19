package com.hims.request;

import lombok.Data;

import java.util.List;
@Data
public class TemplateUpdateRequest {
    private String templateType;
    private Long procedureId;
    private String templateName;
    private List<Long> deleteTemplateDetailsId;
    private List<TemplateItemRequest> templateItemRequests;

}
