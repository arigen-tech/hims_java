package com.hims.service;

import com.hims.request.MasAnaesthesiaTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAnaesthesiaTypeResponse;

import java.util.List;

public interface MasAnaesthesiaTypeService {

    ApiResponse<List<MasAnaesthesiaTypeResponse>> getAll(int flag);

    ApiResponse<MasAnaesthesiaTypeResponse> getById(Long id);

    ApiResponse<MasAnaesthesiaTypeResponse> create(MasAnaesthesiaTypeRequest request);

    ApiResponse<MasAnaesthesiaTypeResponse> update(Long id, MasAnaesthesiaTypeRequest request);

    ApiResponse<MasAnaesthesiaTypeResponse> changeStatus(Long id, String status);
}
