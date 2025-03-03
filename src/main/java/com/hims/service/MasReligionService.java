package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;

import java.util.List;

public interface MasReligionService {
    ApiResponse<List<MasReligionResponse>> getAllReligions();
}
