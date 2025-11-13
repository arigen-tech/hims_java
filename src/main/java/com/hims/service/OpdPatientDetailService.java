package com.hims.service;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.ApiResponse;

import java.util.List;

public interface OpdPatientDetailService {
    ApiResponse<OpdPatientDetail> createOpdPatientDetail(OpdPatientDetailFinalRequest request);

    ApiResponse<List<Visit>> getActiveVisits();
}
