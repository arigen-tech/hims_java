package com.hims.service;

import com.hims.request.OpdEntDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdEntDetailsResponse;
import jakarta.validation.Valid;

public interface OpdEntDetailsService {
    ApiResponse<String> saveEntDetails(@Valid OpdEntDetailsRequest request);

    ApiResponse<OpdEntDetailsResponse> getEntDetails(Long visitId);
}
