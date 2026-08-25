package com.hims.service;

import com.hims.entity.OtBookingRequestDt;
import com.hims.entity.OtBookingRequestHd;
import com.hims.request.OtBookingRequestHdDto;
import com.hims.response.ApiResponse;

public interface OtBookingService {

    ApiResponse<String> createOrUpdateOtBookingHeader(OtBookingRequestHdDto otBookingRequestDto);

}
