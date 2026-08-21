package com.hims.service;

import com.hims.response.ApiResponse;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OtDayAllocationService {
    ApiResponse<String> checkOtAvailability(Long departmentId, Long otId, LocalDate date, LocalTime startTime);
}
