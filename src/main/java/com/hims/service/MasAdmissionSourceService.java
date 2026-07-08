package com.hims.service;

import com.hims.request.MasAdmissionSourceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionSourceResponse;

import java.util.List;

public interface MasAdmissionSourceService {
    ApiResponse<List<MasAdmissionSourceResponse>> getAllMasAdmissionSource(int flag);

    ApiResponse<MasAdmissionSourceResponse> getByIdMasAdmissionSource(Long id);

    ApiResponse<MasAdmissionSourceResponse> createMasAdmissionSource(MasAdmissionSourceRequest request);

    ApiResponse<MasAdmissionSourceResponse> updateMasAdmissionSource(Long id, MasAdmissionSourceRequest request);

    ApiResponse<MasAdmissionSourceResponse> changeStatusMasAdmissionSource(Long id, String status);

}
