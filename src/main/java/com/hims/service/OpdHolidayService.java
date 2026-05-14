package com.hims.service;

import com.hims.request.OpdHolidayMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdHolidayMasterResponse;

import java.util.List;

public interface OpdHolidayService {
    ApiResponse<List<OpdHolidayMasterResponse>> getAllHoliday(int flag);

    ApiResponse<OpdHolidayMasterResponse> getHolidayById(Long id);

    ApiResponse<OpdHolidayMasterResponse> createHoliday(OpdHolidayMasterRequest request);

    ApiResponse<OpdHolidayMasterResponse> updateHoliday(Long id, OpdHolidayMasterRequest request);

    ApiResponse<OpdHolidayMasterResponse> changeStatus(Long id, String status);
}
