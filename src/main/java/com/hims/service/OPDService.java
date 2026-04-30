package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.OpdPreConsultationResponse;
import com.hims.response.PatientWaitingListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OPDService {

    /**
     * Retrieves pending pre-consultations with database-level pagination.
     * 
     * @param pageable the pagination information (page number, size, sorting)
     * @return ApiResponse containing paginated pending pre-consultations
     */

    ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(Pageable pageable, String patientName, String mobileNumber);


    ApiResponse<Page<PatientWaitingListResponse>> getWaitingList(Pageable pageable, String patientName, String mobileNumber);
}