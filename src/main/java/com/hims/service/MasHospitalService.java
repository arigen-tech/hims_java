package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;

import java.util.List;

public interface MasHospitalService {
    ApiResponse<List<MasHospitalResponse>> getAllHospitals();
}
