package com.hims.service;

import com.hims.request.MasSurgeryTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryTypeResponse;

import java.util.List;

public interface MasSurgeryTypeService {
    ApiResponse<List<MasSurgeryTypeResponse>> getAllMasSurgeryType(int flag);

    ApiResponse<MasSurgeryTypeResponse> getByIdMasSurgeryType(Long id);

    ApiResponse<MasSurgeryTypeResponse> createMasSurgeryType(MasSurgeryTypeRequest request);

    ApiResponse<MasSurgeryTypeResponse> updateMasSurgeryType(Long id, MasSurgeryTypeRequest request);

    ApiResponse<MasSurgeryTypeResponse> changeStatusMasSurgeryType(Long id, String status);
}