package com.hims.service;

import com.hims.request.MasToothConditionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasToothConditionResponse;

import java.util.List;

public interface MasToothConditionService {

    ApiResponse<List<MasToothConditionResponse>> getAll(int flag);

    ApiResponse<MasToothConditionResponse> getById(Long id);

    ApiResponse<MasToothConditionResponse> create(
            MasToothConditionRequest request);

    ApiResponse<MasToothConditionResponse> update(
            Long id, MasToothConditionRequest request);

    ApiResponse<MasToothConditionResponse> changeStatus(
            Long id, String status);
}
