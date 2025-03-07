package com.hims.service;

import com.hims.request.MasReligionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;

import java.util.List;

public interface MasReligionService {
    ApiResponse<List<MasReligionResponse>> getAllReligions();
    public ApiResponse<MasReligionResponse> addReligion(MasReligionRequest religionRequest);
    public ApiResponse<MasReligionResponse> updateReligion(Long id, MasReligionResponse religionDetails);
    public ApiResponse<MasReligionResponse> changeStatus(Long id, String status);
    public ApiResponse<MasReligionResponse> findById(Long id);

}
