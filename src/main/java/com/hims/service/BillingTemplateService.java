package com.hims.service;

import com.hims.request.TemplateRequest;
import com.hims.request.TemplateUpdateRequest;
import com.hims.response.*;
import org.springframework.data.domain.Page;

public interface BillingTemplateService {
    ApiResponse<String> saveBillingTemplate(TemplateRequest request);

    ApiResponse<String> changeStatusBillingTemplate(Long id, String status);

    ApiResponse<String> updateBillingTemplate(Long templateId, TemplateUpdateRequest request);

    ApiResponse<BillingTemplateResponse> getByIdBillingTemplate(Long id);

    ApiResponse<Page<BillingTemplateSearchResponse>> searchTemplates(String templateType, String templateName, int page, int size);


    ApiResponse<Page<?>> searchProcedureAndSurgery(String templateType, int page, int size, String search);
}
