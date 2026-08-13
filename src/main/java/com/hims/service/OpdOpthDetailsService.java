package com.hims.service;

import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdTemplateResponse;
import com.hims.response.OphthalmologyExaminationDetailResponse;

public interface OpdOpthDetailsService {
    ApiResponse<String> opdVisionExaminationDetailsSaveOrUpdate(OpdOpthDetailsRequest request);

    ApiResponse<OphthalmologyExaminationDetailResponse> getOphthalmologyExaminationDetail(Long visitId);
}
