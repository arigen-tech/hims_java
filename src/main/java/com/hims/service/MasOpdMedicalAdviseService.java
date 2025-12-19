package com.hims.service;

import com.hims.request.MasOpdMedicalAdviseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdMedicalAdviseResponse;

import java.util.List;

public interface MasOpdMedicalAdviseService {
    ApiResponse<List<MasOpdMedicalAdviseResponse>> getAll(int flag);



    ApiResponse<MasOpdMedicalAdviseResponse> create(MasOpdMedicalAdviseRequest request);

    ApiResponse<MasOpdMedicalAdviseResponse> update(Long id, MasOpdMedicalAdviseRequest request);

    ApiResponse<MasOpdMedicalAdviseResponse> changeStatus(Long id, String status);
}
