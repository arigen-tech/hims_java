package com.hims.service;

import com.hims.request.MasSpecialtyCenterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSpecialtyCenterResponse;

import java.util.List;

public interface MasSpecialtyCenterService {

    ApiResponse<List<MasSpecialtyCenterResponse>> getAll(int flag);

    ApiResponse<MasSpecialtyCenterResponse> getById(Long id);

    ApiResponse<MasSpecialtyCenterResponse> create(
            MasSpecialtyCenterRequest request);

    ApiResponse<MasSpecialtyCenterResponse> update(
            Long id, MasSpecialtyCenterRequest request);

    ApiResponse<MasSpecialtyCenterResponse> changeStatus(
            Long id, String status);
}
