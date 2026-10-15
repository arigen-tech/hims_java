package com.hims.service;

import com.hims.projection.VisitSummaryProjection;
import com.hims.response.ApiResponse;

import java.util.List;

public interface DoctorService {

    ApiResponse<List<VisitSummaryProjection>> getTodayVisitSummary(Long hospitalId,
                                     Long doctorId,
                                     Long departmentId
    );
}
