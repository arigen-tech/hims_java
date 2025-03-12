package com.hims.service;

import com.hims.request.TemplateApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.TemplateApplicationResponse;

import java.util.List;

public interface TemplateApplicationService {
    ApiResponse<TemplateApplicationResponse> assignTemplateToApplication(TemplateApplicationRequest request);
    ApiResponse<String> changeTemplateApplicationStatus(Long id, String status);
    ApiResponse<List<TemplateApplicationResponse>> getAllTemplateApplications(int flag);
}
