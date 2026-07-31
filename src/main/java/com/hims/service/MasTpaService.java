package com.hims.service;

import com.hims.request.MasTpaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTpaResponse;

import java.util.List;

public interface MasTpaService {
    ApiResponse<List<MasTpaResponse>> getAllMasTpa(int flag);
    ApiResponse<MasTpaResponse> getByIdTpa(Long id);

    ApiResponse<MasTpaResponse> createTpa(MasTpaRequest request);

    ApiResponse<MasTpaResponse> updateTpa(Long id, MasTpaRequest request);

    ApiResponse<MasTpaResponse> changeStatusTpa(Long id, String status);
}
