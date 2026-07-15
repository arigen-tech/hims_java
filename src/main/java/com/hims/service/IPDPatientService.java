package com.hims.service;

import com.hims.request.IpNursingMedicalAssessmentRequest;
import com.hims.request.IpdPatientRequest;
import com.hims.response.*;
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

    ApiResponse<List<IpdWardResponse>> getWardByDepartment(Long departmentId);

    ApiResponse<List<IpdRoomResponse>> getRoomByWard(Long wardId);

    ApiResponse<List<WardResponse>> getWardByCategory(Long wardCategoryId);

    ApiResponse<List<BedResponse>> getBedByRoom(Long roomId);

    ApiResponse<List<WardWiseDetailsResponse>> getWardWiseDetails(Long departmentId);

    ApiResponse<TotalBedCountResponse> getTotalBedCount(Long departmentId);

    ApiResponse<String> SaveIpNursingMedicalAssessment(IpNursingMedicalAssessmentRequest request);
}
