package com.hims.service;

import com.hims.request.MasDistrictRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;

import java.util.List;

public interface MasDistrictService {
    ApiResponse<MasDistrictResponse> addDistrict(MasDistrictRequest request);
    ApiResponse<String> changeDistrictStatus(Long id, String status);
    ApiResponse<MasDistrictResponse> editDistrict(Long id, MasDistrictRequest request);
    ApiResponse<MasDistrictResponse> getDistrictById(Long id);
    ApiResponse<List<MasDistrictResponse>> getAllDistricts();
    ApiResponse<List<MasDistrictResponse>> getDistrictsByStateId(Long stateId);
}
