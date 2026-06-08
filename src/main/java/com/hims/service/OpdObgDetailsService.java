package com.hims.service;

import com.hims.request.OpdObgDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdObgDetailsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


public interface OpdObgDetailsService {
    ApiResponse<String> saveObgDetails(@Valid OpdObgDetailsRequest request);

    /**
     * Retrieve OBG examination details for a specific visit
     * Fetches data using projection and maps to response class
     * @param visitId the visit ID to retrieve details for
     * @return ApiResponse containing OBG details response or error message
     */
    ApiResponse<OpdObgDetailsResponse> getObgDetailsByVisitId(Long visitId);
}
