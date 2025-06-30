package com.hims.service;

import com.hims.entity.MasGender;
import com.hims.request.MasGenderRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;

import java.util.List;

public interface MasGenderService {
    ApiResponse<List<MasGenderResponse>> getAllGenders(int flag);
    public ApiResponse<MasGenderResponse> addGender(MasGenderRequest genderRequest);
    public ApiResponse<MasGenderResponse> updateGender(Long id, MasGenderResponse genderDetails);
    public ApiResponse<MasGenderResponse> changeGenderStatus(Long id, String status);
    public ApiResponse<MasGenderResponse> findById(Long id);

}
