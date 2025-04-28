package com.hims.service;

import com.hims.request.BatchUpdateRequest;
import com.hims.request.MasApplicationRequest;
import com.hims.request.UpdateStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;

import java.util.List;

public interface MasApplicationService {

    ApiResponse<List<MasApplicationResponse>> getAllApplications(int flag);
    ApiResponse<MasApplicationResponse> getApplicationById(String id);
    ApiResponse<MasApplicationResponse> createApplication(MasApplicationRequest request);
    ApiResponse<MasApplicationResponse> updateApplication(String id, MasApplicationRequest request);
    public ApiResponse<List<MasApplicationResponse>> getAllByParentId(String parentId, Long templateId);
    ApiResponse<String> updateMultipleApplicationStatuses(UpdateStatusRequest request);
    ApiResponse<List<MasApplicationResponse>> getAllParentApplications(int flag);
    ApiResponse<String> processBatchUpdates(BatchUpdateRequest request);
}
