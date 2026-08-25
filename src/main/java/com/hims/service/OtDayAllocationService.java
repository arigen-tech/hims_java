package com.hims.service;

import com.hims.request.OtDayAllocationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtDayAllocationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface OtDayAllocationService {
    ApiResponse<String> checkOtAvailability(Long departmentId, Long otId, LocalDate date);

    ApiResponse<String> saveOtDayAllocation(OtDayAllocationRequest request);

    ApiResponse<List<OtDayAllocationResponse>> getAllOtDayAllocations(int flag);

    ApiResponse<OtDayAllocationResponse> getById(Long id);

    ApiResponse<OtDayAllocationResponse> changeStatus(Long id, String status);

    ApiResponse<String> updateOtDayAllocation(Long id, OtDayAllocationRequest request);
}