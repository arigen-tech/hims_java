package com.hims.service;

import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserApplicationResponse;

import java.util.List;

public interface UserApplicationService {

    ApiResponse<List<UserApplicationResponse>> getAllApplications(int flag);
    ApiResponse<UserApplicationResponse> getApplicationById(Long id);
    ApiResponse<UserApplicationResponse> createApplication(UserApplicationRequest request);
    ApiResponse<UserApplicationResponse> updateApplication(Long id, UserApplicationRequest request);
    ApiResponse<String> changeApplicationStatus(Long id, String status);
    ApiResponse<List<UserApplicationResponse>> getAllApplicationsWithHashUrl(int flag);

}
