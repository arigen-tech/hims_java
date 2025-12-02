package com.hims.service;

import com.hims.request.MasBedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedStatusResponse;

import java.util.List;

public interface MasBedStatusService {

    ApiResponse<MasBedStatusResponse> createBedStatus(MasBedStatusRequest request);

    ApiResponse<MasBedStatusResponse> updateBedStatus(Long bedStatusId, MasBedStatusRequest request);

    ApiResponse<MasBedStatusResponse> changeActiveStatus(Long bedStatusId, String status);

    ApiResponse<MasBedStatusResponse> getById(Long bedStatusId);

    ApiResponse<List<MasBedStatusResponse>> getAll(int flag);
}
