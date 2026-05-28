package com.hims.service;

import com.hims.request.MasDrugScheduleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDrugScheduleResponse;

import java.util.List;

public interface MasDrugScheduleService {
    ApiResponse<List<MasDrugScheduleResponse>> getAllSchedule(int flag);

    ApiResponse<MasDrugScheduleResponse> getScheduleById(String id);

    ApiResponse<MasDrugScheduleResponse> createSchedule(
            MasDrugScheduleRequest request);

    ApiResponse<MasDrugScheduleResponse> updateSchedule(
            String id,
            MasDrugScheduleRequest request);

    ApiResponse<MasDrugScheduleResponse> changeStatus(
            String id,
            String status);
}
