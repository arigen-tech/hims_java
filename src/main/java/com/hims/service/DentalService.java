package com.hims.service;

import com.hims.entity.Patient;
import com.hims.entity.Visit;
import com.hims.request.DentalDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DentalProcedureHistoryResponse;
import com.hims.response.DentalSummaryResponse;
import com.hims.response.ProcedureSessionHistoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DentalService {

    ApiResponse<String> createOrUpdateDentalDetails(
            DentalDetailsRequest request,
            Patient patient,
            Visit visit,
            Long departmentId
    );

    ApiResponse<Page<DentalSummaryResponse>> getPreviousDentalDetails(Long patientId,int page,int size);

    ApiResponse<Page<DentalProcedureHistoryResponse>> getDentalProcedureHistory(Long patientId, int page, int size);

   ApiResponse< List<ProcedureSessionHistoryResponse>> getProcedureSessionHistory(
            Long procedureDtId
    );
}