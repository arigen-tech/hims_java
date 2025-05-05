package com.hims.service;

import com.hims.request.MasReligionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;

import java.util.List;

public interface MasReligionService {
    ApiResponse<List<MasReligionResponse>> getAllReligions(int flag);
    public ApiResponse<MasReligionResponse> addReligion(MasReligionRequest religionRequest);
    public ApiResponse<MasReligionResponse> updateReligion(Long id, MasReligionRequest religionRequest);
    public ApiResponse<MasReligionResponse> changeStatus(Long id, String status);
    public ApiResponse<MasReligionResponse> findById(Long id);
}
