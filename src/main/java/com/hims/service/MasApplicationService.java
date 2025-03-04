package com.hims.service;

import com.hims.request.MasApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;

import java.util.List;

public interface MasApplicationService {

    ApiResponse<List<MasApplicationResponse>> getAllApplications();
    ApiResponse<MasApplicationResponse> getApplicationById(String id);
    ApiResponse<MasApplicationResponse> createApplication(MasApplicationRequest request);
    ApiResponse<MasApplicationResponse> updateApplication(String id, MasApplicationRequest request);
    ApiResponse<String> changeApplicationStatus(String id, String status);
}
