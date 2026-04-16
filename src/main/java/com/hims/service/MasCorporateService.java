package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasCorporateResponse;
import lombok.Data;

import java.util.List;

public interface MasCorporateService {
    ApiResponse<List<MasCorporateResponse>> getAllMasCorporate(int flag);
}
