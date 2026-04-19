package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class BillingTemplateResponse {
    private Long templateId;
    private String templateType;
    private String procedureName;
    private String templateName;
    List<BillingTemplateDetailItemResponse> billingTemplateDetailItemResponseList;
}
