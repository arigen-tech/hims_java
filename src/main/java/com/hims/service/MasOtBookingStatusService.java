package com.hims.service;



import com.hims.request.MasOtBookingStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtBookingStatusResponse;

import java.util.List;

public interface MasOtBookingStatusService {

    ApiResponse<String> saveOtBookingStatus(MasOtBookingStatusRequest request);

    ApiResponse<List<MasOtBookingStatusResponse>> getAllOtBookingStatus(int flag);

    ApiResponse<MasOtBookingStatusResponse> getById(Long id);

    ApiResponse<MasOtBookingStatusResponse> changeStatus(Long id, String status);

    ApiResponse<String> updateOtBookingStatus(Long id, MasOtBookingStatusRequest request);
}