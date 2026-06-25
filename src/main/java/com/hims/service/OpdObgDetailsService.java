package com.hims.service;

import com.hims.request.OpdObgDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdObgDetailsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


public interface OpdObgDetailsService {
    /**
     * Create or Update OBG examination details for a visit
     * Since one visit can have only one OBG details record,
     * this method checks if record exists and updates, otherwise creates new
     * @param visitId the visit ID (unique key for OBG details)
     * @param request the OBG details request with examination data
     * @return ApiResponse with success/error message
     */
    ApiResponse<String> createOrUpdateObgDetails(Long visitId, @Valid OpdObgDetailsRequest request);

    /**
     * Retrieve OBG examination details for a specific visit
     * Fetches data using projection and maps to response class
     * @param visitId the visit ID to retrieve details for
     * @return ApiResponse containing OBG details response or error message
     */
    ApiResponse<OpdObgDetailsResponse> getObgDetailsByVisitId(Long visitId);
}
