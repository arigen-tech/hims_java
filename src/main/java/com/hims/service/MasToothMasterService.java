package com.hims.service;

import com.hims.request.MasToothMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasToothMasterResponse;

import java.util.List;

public interface MasToothMasterService {
    ApiResponse<List<MasToothMasterResponse>> getAll(int flag);

    ApiResponse<MasToothMasterResponse> getById(Long id);

    ApiResponse<MasToothMasterResponse> create(MasToothMasterRequest request);

    ApiResponse<MasToothMasterResponse> update(Long id, MasToothMasterRequest request);

    ApiResponse<MasToothMasterResponse> changeStatus(Long id, String status);
}
