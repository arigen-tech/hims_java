package com.hims.service;

import com.hims.request.MasTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTemplateResponse;

import java.util.List;

public interface MasTemplateService {

    ApiResponse<List<MasTemplateResponse>> getAllTemplates();
    ApiResponse<MasTemplateResponse> getTemplateById(Long id);
    ApiResponse<MasTemplateResponse> createTemplate(MasTemplateRequest request);
    ApiResponse<MasTemplateResponse> updateTemplate(Long id, MasTemplateRequest request);
    ApiResponse<String> changeTemplateStatus(Long id, String status);
}
