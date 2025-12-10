package com.hims.service;

import com.hims.request.MasDietScheduleStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietScheduleStatusResponse;

import java.util.List;

public interface MasDietScheduleStatusService {

    ApiResponse<List<MasDietScheduleStatusResponse>> getAll(int flag);

    ApiResponse<MasDietScheduleStatusResponse> getById(Long id);

    ApiResponse<MasDietScheduleStatusResponse> create(MasDietScheduleStatusRequest request);

    ApiResponse<MasDietScheduleStatusResponse> update(Long id, MasDietScheduleStatusRequest request);

    ApiResponse<MasDietScheduleStatusResponse> changeStatus(Long id, String status);
}
