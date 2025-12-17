package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasOpdMedicalAdviseResponse;

import java.util.List;

public interface MasOpdMedicalAdviseService {
    ApiResponse<List<MasOpdMedicalAdviseResponse>> getAll(int flag);
}
