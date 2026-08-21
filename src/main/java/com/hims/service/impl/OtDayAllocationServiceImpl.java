package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.repository.OtDayAllocationRepository;
import com.hims.response.ApiResponse;
import com.hims.service.OtDayAllocationService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtDayAllocationServiceImpl implements OtDayAllocationService {

    private final OtDayAllocationRepository repository;

    @Override
    public ApiResponse<String> checkOtAvailability(Long departmentId, Long otId, LocalDate date, LocalTime startTime) {
        try {
            if (departmentId == null || otId == null || date == null || startTime == null ) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "All parameters (departmentId, otId, date, startTime, endTime) are required",
                        HttpStatus.BAD_REQUEST.value());
            }

            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            var allocation = repository.findByOtAndDepartmentAndDayAndTimeRange(otId, departmentId, dayOfWeek, startTime, AppConstants.STATUS_Y.toLowerCase());

            if (allocation.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "The OT is not configured for this day",
                        HttpStatus.NOT_FOUND.value());
            }

            return ResponseUtils.createSuccessResponse("OT is available and configured for the requested time slot",
                    new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error checking OT availability", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
