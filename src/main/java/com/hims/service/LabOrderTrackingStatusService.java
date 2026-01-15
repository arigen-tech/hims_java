package com.hims.service;

import com.hims.request.LabOrderTrackingStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.LabOrderTrackingStatusResponse;

public interface LabOrderTrackingStatusService {

    ApiResponse<LabOrderTrackingStatusResponse> create(LabOrderTrackingStatusRequest request);
}
