package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasOtTypeResponse;

import java.util.List;

public interface MasOtTypeService {

    ApiResponse<List<MasOtTypeResponse>> getAllOtTypes(int flag);

    ApiResponse<MasOtTypeResponse> getById(Long id);
}
