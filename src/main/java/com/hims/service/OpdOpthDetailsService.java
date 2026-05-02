package com.hims.service;

import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdTemplateResponse;

public interface OpdOpthDetailsService {
    ApiResponse<String> opdVisionExaminationDetailsSave(OpdOpthDetailsRequest request);
}
