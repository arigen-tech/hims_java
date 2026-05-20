package com.hims.service;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.request.ActiveVisitSearchRequest;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
@Service
public interface OpdPatientDetailService {
    ApiResponse<OpdPatientVitalResponse> getOpdPatientByVisit(Long visitId);


    /**
     * Creates OPD patient detail with comprehensive billing structure.
     *
     * Creates a new OPD patient registration along with associated order header,
     * order details, billing header, and billing details. Follows the same
     * structure as lab registration for consistency.
     *
     * @param request the OPD patient detail request containing patient, order, and billing information
     * @return ApiResponse containing the created OpdPatientDetailResponseDTO with order and billing IDs
     */
    @Transactional
    ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetail(OpdPatientDetailFinalRequest request);


//    @Transactional
//    ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetailWithBilling(OpdPatientDetailFinalRequest request);

    @Transactional
    ApiResponse<String> recallOpdPatientDetail(RecallOpdPatientDetailRequest request);

    ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisits();

    ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisitsWithFilters(ActiveVisitSearchRequest req);

    ApiResponse<OpdPatientRecallResponce> getRecallVisit(Long visitId);

    ApiResponse<String> updateVisitStatus(Long visitId, String status);


    Visit updateVisitStatus(Long visitId, Instant visitDate, Long doctorId);

    /**
     * Retrieves pending pre-consultations with database-level pagination.
     *
     * @param pageable the pagination information (page number, size, sorting)
     * @return ApiResponse containing paginated pending pre-consultations
     */
    ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(Pageable pageable, String patientName, String mobileNumber);


    ApiResponse<Page<PatientWaitingListResponse>> getWaitingList(Pageable pageable, String patientName, String mobileNumber, Long doctorId, Long sessionId);

    ApiResponse<List<PrescriptionDetailResponse>> getPrescriptionDetailsByPatientId(Long patientId);


    ApiResponse<Page<PreviousOpdVisitResponse>> getPreviousOpdVisit(
            Long patientId, Long hospitalId, int page, int size);

    ApiResponse<Page<PreviousOpdVitalsDetailsResponse>> getPreviousOpdVitalsDetailsHistory(Long patientId, Long hospitalId, int page, int size);


    ApiResponse<Page<OpdRecallVisitResponse>> getRecallOpdVisit(String name, String mobile, LocalDate visitDate, int page, int size);
}
