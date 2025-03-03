package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;

import java.util.List;

public interface MasDistrictService {
    ApiResponse<List<MasDistrictResponse>> getAllDistricts();
}
