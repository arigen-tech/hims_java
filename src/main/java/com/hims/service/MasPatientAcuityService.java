package com.hims.service;

import com.hims.request.MasPatientAcuityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientAcuityResponse;

import java.util.List;

public interface MasPatientAcuityService {
    ApiResponse<List<MasPatientAcuityResponse>> getAll(int flag);

    ApiResponse<MasPatientAcuityResponse> getById(Long id);

    ApiResponse<MasPatientAcuityResponse> create(MasPatientAcuityRequest request);

    ApiResponse<MasPatientAcuityResponse> update(Long id, MasPatientAcuityRequest request);

    ApiResponse<MasPatientAcuityResponse> changeStatus(Long id, String status);
}
