package com.hims.service;

import com.hims.request.OtChecklistSubmitRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtChecklistSubmitResponse;
import java.util.List;

public interface OtChecklistSubmissionService {
    ApiResponse<OtChecklistSubmitResponse> submit(OtChecklistSubmitRequest request);
    ApiResponse<List<OtChecklistSubmitResponse>> getByBookingId(Long otBookingId);
}
