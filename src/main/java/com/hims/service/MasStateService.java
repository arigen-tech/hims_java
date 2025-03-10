package com.hims.service;

import com.hims.request.MasStateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;

import java.util.List;

public interface MasStateService {
    ApiResponse<MasStateResponse> addState(MasStateRequest request);
    ApiResponse<String> changeStateStatus(Long id, String status);
    ApiResponse<MasStateResponse> editState(Long id, MasStateRequest request);
    ApiResponse<MasStateResponse> getStateById(Long id);
    ApiResponse<List<MasStateResponse>> getAllStates();
    ApiResponse<List<MasStateResponse>> getStatesByCountryId(Long countryId);
}
