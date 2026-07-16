package com.hims.service;

import com.hims.request.IpNursingMedicalAssessmentRequest;
import com.hims.request.IpdPatientRequest;
import com.hims.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IPDPatientService {
    ApiResponse<Page<IPDPatientWaitingListResponse>> pendingAdmissionList(
            int page,
            int size,
            Long hospitalId,
            String patientName,
            String mobileNo
    );

    ApiResponse<String> saveAdmissionDetails(IpdPatientRequest request);

    ApiResponse<List<IpdWardResponse>> getWardDetailsByDepartment(Long departmentId);

    ApiResponse<List<IpdRoomResponse>> getRoomDetailsByWard(Long wardId);

    ApiResponse<List<WardResponse>> getWardDetailsByCategory(Long wardCategoryId);

    ApiResponse<List<BedResponse>> getBedDetailsByRoom(Long roomId);

    ApiResponse<List<WardWiseDetailsResponse>> getNursingDashboardByWard(Long wardId);

    ApiResponse<TotalBedCountResponse> getTotalBedCountByWard(Long wardId);

    ApiResponse<String> saveNursingMedicalAssessment(IpNursingMedicalAssessmentRequest request);

    ApiResponse<String> updateAdmissionInternalStatus(Long inpatientId,Long internalStatusId);
}
