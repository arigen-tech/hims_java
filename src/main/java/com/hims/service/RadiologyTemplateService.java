package com.hims.service;

import com.hims.request.RadiologyTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.RadiologyTemplateResponse;

import java.util.List;

public interface RadiologyTemplateService {
    ApiResponse<List<RadiologyTemplateResponse>> getAll(int flag);

    ApiResponse<RadiologyTemplateResponse> getById(Long id);

    ApiResponse<RadiologyTemplateResponse> create(RadiologyTemplateRequest request);

    ApiResponse<RadiologyTemplateResponse> update(Long id, RadiologyTemplateRequest request);

    ApiResponse<RadiologyTemplateResponse> changeStatus(Long id, String status);
}
