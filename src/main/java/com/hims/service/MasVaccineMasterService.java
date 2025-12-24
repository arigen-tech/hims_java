package com.hims.service;

import com.hims.request.MasVaccineMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasVaccineMasterResponse;

import java.util.List;

public interface MasVaccineMasterService {

    ApiResponse<List<MasVaccineMasterResponse>> getAll(int flag);

    ApiResponse<MasVaccineMasterResponse> getById(Long id);

    ApiResponse<MasVaccineMasterResponse> create(
            MasVaccineMasterRequest request);

    ApiResponse<MasVaccineMasterResponse> update(
            Long id, MasVaccineMasterRequest request);

    ApiResponse<MasVaccineMasterResponse> changeStatus(
            Long id, String status);
}
