package com.hims.service;

import com.hims.request.OpdQuestionMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdQuestionMasterResponse;

import java.util.List;

public interface OpdQuestionMasterService {
    ApiResponse<List<OpdQuestionMasterResponse>> getAll(int flag);

    ApiResponse<OpdQuestionMasterResponse> getById(Long id);

    ApiResponse<OpdQuestionMasterResponse> create(OpdQuestionMasterRequest request);

    ApiResponse<OpdQuestionMasterResponse> update(Long id, OpdQuestionMasterRequest request);

    ApiResponse<OpdQuestionMasterResponse> changeStatus(Long id, String status);
}
