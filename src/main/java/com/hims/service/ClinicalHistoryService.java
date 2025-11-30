package com.hims.service;

import com.hims.entity.Visit;
import com.hims.response.ApiResponse;
import com.hims.response.VisitResponse;

import java.util.List;

public interface ClinicalHistoryService {
    ApiResponse<List<VisitResponse>> getPreviousVisits(Integer patient);
}
