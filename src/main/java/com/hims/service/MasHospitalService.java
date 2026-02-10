package com.hims.service;

import com.hims.request.MasHospitalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.response.MasHospitalResponse2;

import java.util.List;

public interface MasHospitalService {
    ApiResponse<List<MasHospitalResponse>> getAllHospitals(int flag);
    ApiResponse<MasHospitalResponse> changeHospitalStatus(Long id, String status);
    ApiResponse<MasHospitalResponse> findById(Long id);
    ApiResponse<MasHospitalResponse> addHospital(MasHospitalRequest hospitalRequest);
    ApiResponse<MasHospitalResponse> updateHospital(Long id, MasHospitalRequest hospitalRequest);

    ApiResponse<List<MasHospitalResponse2>> getAllHospitalsResponse(int flag);
}
