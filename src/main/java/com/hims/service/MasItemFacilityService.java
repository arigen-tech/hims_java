package com.hims.service;

import com.hims.request.MasItemFacilityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemFacilityResponse;

import java.util.List;

public interface MasItemFacilityService {
    ApiResponse<List<MasItemFacilityResponse>> getAllFacility(int flag);

    ApiResponse<MasItemFacilityResponse> getFacilityById(Long id);

    ApiResponse<MasItemFacilityResponse> createFacility(MasItemFacilityRequest request);

    ApiResponse<MasItemFacilityResponse> updateFacility(Long id,
                                                        MasItemFacilityRequest request);

    ApiResponse<MasItemFacilityResponse> changeStatus(Long id,
                                                      String status);
}
