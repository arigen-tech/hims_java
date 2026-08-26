package com.hims.service;

import com.hims.request.OtBookingRequestHdDto;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ApiResponse;
import org.springframework.data.domain.Page;

public interface OtService {

    ApiResponse<String> createOrUpdateOtBookingHeader(OtBookingRequestHdDto otBookingRequestDto);

    ApiResponse<Page<ActiveAdmissionOtResponse>> activeAdmissionList(int page, int size, String patientName, String mobileNo, String admissionNo, Long wardId);
}
