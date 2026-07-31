package com.hims.projection;
public interface BillingTemplateProjection {
    Long getTemplateId();
    String getTemplateType();
    String getTemplateName();
    String getProcedureName();
    Long getProcedureId();
    Long getSurgeryId();
}