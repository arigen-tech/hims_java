package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hims.entity.repository.DashboardRepository;
import com.hims.response.ApiResponse;
import com.hims.service.DashboardService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<Map<String, Object>> getDashboardData(LocalDate fromDate, LocalDate toDate) {
        return getParsedDashboardResponse(fromDate, toDate, dashboardRepository.getDashboardData(fromDate, toDate));
    }

    @Override
    public ApiResponse<Map<String, Object>> getBillingFinanceDashboardData(LocalDate fromDate, LocalDate toDate) {
        return getParsedDashboardResponse(fromDate, toDate, dashboardRepository.getBillingFinanceDashboardData(fromDate, toDate));
    }

    private ApiResponse<Map<String, Object>> getParsedDashboardResponse(LocalDate fromDate, LocalDate toDate, String response) {
        try {
            if (fromDate == null || toDate == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "fromDate and toDate are required",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            if (fromDate.isAfter(toDate)) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "fromDate cannot be greater than toDate",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            Map<String, Object> parsedResponse = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>() {}
            );

            return ResponseUtils.createSuccessResponse(parsedResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching dashboard data for fromDate {} and toDate {}", fromDate, toDate, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Unable to fetch dashboard data",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
