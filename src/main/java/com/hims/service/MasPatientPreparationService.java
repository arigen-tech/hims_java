package com.hims.service;

import com.hims.request.MasPatientPreparationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientPreparationResponse;

import java.util.List;

public interface MasPatientPreparationService {

    ApiResponse<MasPatientPreparationResponse> create(MasPatientPreparationRequest request);

    ApiResponse<MasPatientPreparationResponse> update(Long preparationId, MasPatientPreparationRequest request);

    ApiResponse<MasPatientPreparationResponse> changeActiveStatus(Long preparationId, String status);

    ApiResponse<MasPatientPreparationResponse> getById(Long preparationId);

    ApiResponse<List<MasPatientPreparationResponse>> getAll(int flag);
}
