package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.OpdPreConsultationResponse;
import com.hims.response.PatientWaitingListResponse;

import java.util.List;

public interface OPDService {

    ApiResponse<List<OpdPreConsultationResponse>> getPendingPreConsultations();

    ApiResponse<List<PatientWaitingListResponse>> getWaitingList();
}