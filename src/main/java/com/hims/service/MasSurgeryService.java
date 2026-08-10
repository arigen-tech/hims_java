package com.hims.service;

import com.hims.request.MasSurgeryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryResponse;

import java.util.List;

public interface MasSurgeryService {
    ApiResponse<List<MasSurgeryResponse>> getAllMasSurgery(int flag);

    ApiResponse<MasSurgeryResponse> getByIdMasSurgery(Long id);

    ApiResponse<MasSurgeryResponse> createMasSurgery(MasSurgeryRequest request);

    ApiResponse<MasSurgeryResponse> updateMasSurgery(Long id, MasSurgeryRequest request);

    ApiResponse<MasSurgeryResponse> changeStatusMasSurgery(Long id, String status);
}