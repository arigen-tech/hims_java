package com.hims.service;

import com.hims.request.IpdPatientRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IPDPatientWaitingListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IPDPatientService {
    ApiResponse<Page<IPDPatientWaitingListResponse>> ipdPatientWaitingList(
            int page,
            int size,
            Long hospitalId,
            String patientName,
            String mobileNo
    );

    ApiResponse<String> saveIpdPatientDetails(IpdPatientRequest request);
}
