package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.PatientVisitQueueResponse;
import com.hims.response.PrevClinicalNotesAndHistoryResponse;
import org.springframework.data.domain.Page;

public interface VisitService {
    ApiResponse<String> changeVisitStatus(Long visitId, String visitStatus);

    ApiResponse<Page<PatientVisitQueueResponse>> getPatientVisitQueue(Long hospitalId, Long doctorId, Long departmentId, int page, int size);

    ApiResponse<PrevClinicalNotesAndHistoryResponse> getPreviousVisitPastHistoryAndClinicalNotes(Long visitId);
}
