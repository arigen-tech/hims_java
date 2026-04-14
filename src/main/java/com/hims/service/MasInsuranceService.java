package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasInsuranceResponse;

import java.util.List;

public interface MasInsuranceService {
    ApiResponse<List<MasInsuranceResponse>> getAllMasInsurance(int flag);
}
