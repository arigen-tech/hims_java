package com.hims.service;

import com.hims.request.MasAdmissionTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionTypeResponse;

import java.util.List;
public interface MasAdmissionTypeService {
    ApiResponse<List<MasAdmissionTypeResponse>> getAll(int flag);

    ApiResponse<MasAdmissionTypeResponse> getById(Long id);

    ApiResponse<MasAdmissionTypeResponse> create(MasAdmissionTypeRequest request);

    ApiResponse<MasAdmissionTypeResponse> update(Long id, MasAdmissionTypeRequest request);

    ApiResponse<MasAdmissionTypeResponse> changeStatus(Long id, String status);
}
