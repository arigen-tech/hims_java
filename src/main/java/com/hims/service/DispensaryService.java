package com.hims.service;

import com.hims.request.PrescriptionHeaderApproveRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PatientPrescriptionDetailsResponse;
import com.hims.response.PatientPrescriptionHeaderResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DispensaryService {

    ApiResponse<Page<PatientPrescriptionHeaderResponse>> getPendingPrescriptionsHeaders(
            Long hospitalId,
            Long departmentId,
            String patientName,
            String patientMobileNo,
            int page,
            int size
    );

    ApiResponse<List<PatientPrescriptionDetailsResponse>> getPendingPrescriptionsDetailsWrtHeader(Long prescriptionHeaderId);

    ApiResponse<String> approvePrescription(PrescriptionHeaderApproveRequest request);

    ApiResponse<String> closePendingPrescription(Long prescriptionHeaderId);
}
