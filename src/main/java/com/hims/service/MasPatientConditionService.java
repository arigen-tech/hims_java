package com.hims.service;

import com.hims.request.MasPatientConditionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientConditionResponse;

import java.util.List;

public interface MasPatientConditionService {
    ApiResponse<List<MasPatientConditionResponse>> getAllMasPatientCondition(int flag);

    ApiResponse<MasPatientConditionResponse> getByIdMasPatientCondition(Long id);

    ApiResponse<MasPatientConditionResponse> createMasPatientCondition(MasPatientConditionRequest request);

    ApiResponse<MasPatientConditionResponse> updateMasPatientCondition(Long id, MasPatientConditionRequest request);

    ApiResponse<MasPatientConditionResponse> changeStatusMasPatientCondition(Long id, String status);
}
