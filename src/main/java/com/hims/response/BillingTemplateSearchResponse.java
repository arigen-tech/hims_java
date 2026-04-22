package com.hims.response;

import lombok.Data;

@Data
public class BillingTemplateSearchResponse {
    private Long templateId;
    private String templateType;
    private String procedure;
    private String templateName;
    private Long itemCount;
    private  String status;
}
