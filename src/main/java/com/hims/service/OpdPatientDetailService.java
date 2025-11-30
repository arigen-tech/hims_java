package com.hims.service;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.request.ActiveVisitSearchRequest;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdPatientDetailsWaitingresponce;
import com.hims.response.OpdPatientRecallResponce;
import com.hims.response.RecallOpdPatientDetailRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface OpdPatientDetailService {
    ApiResponse<OpdPatientDetail> createOpdPatientDetail(OpdPatientDetailFinalRequest request);

    @Transactional
    ApiResponse<OpdPatientDetail> recallOpdPatientDetail(RecallOpdPatientDetailRequest request);

    ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisits();

    ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisitsWithFilters(ActiveVisitSearchRequest req);

    ApiResponse<List<OpdPatientRecallResponce>> getRecallVisit(String name, String mobile, LocalDate visitDate);

    ApiResponse<String> updateVisitStatus(Long visitId, String status);
}
