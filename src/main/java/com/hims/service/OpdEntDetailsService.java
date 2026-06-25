package com.hims.service;

import com.hims.request.OpdEntDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdEntDetailsResponse;
import jakarta.validation.Valid;

public interface OpdEntDetailsService {
    /**
     * Create or Update ENT examination details for a visit
     * Since one visit can have only one ENT details record,
     * this method checks if record exists and updates, otherwise creates new
     * @param visitId the visit ID (unique key for ENT details)
     * @param request the ENT details request with examination data
     * @return ApiResponse with success/error message
     */
    ApiResponse<String> createOrUpdateEntDetails(Long visitId, @Valid OpdEntDetailsRequest request);

    /**
     * Retrieve ENT examination details for a specific visit
     * @param visitId the visit ID to retrieve details for
     * @return ApiResponse containing ENT details response or error message
     */
    ApiResponse<OpdEntDetailsResponse> getEntDetailsByVisit(Long visitId);
}
