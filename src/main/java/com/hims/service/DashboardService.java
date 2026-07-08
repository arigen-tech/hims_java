package com.hims.service;

import com.hims.response.ApiResponse;

import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {

    ApiResponse<Map<String, Object>> getDashboardData(LocalDate fromDate, LocalDate toDate);

    ApiResponse<Map<String, Object>> getBillingFinanceDashboardData(LocalDate fromDate, LocalDate toDate);
}
