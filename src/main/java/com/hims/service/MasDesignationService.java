package com.hims.service;

import com.hims.request.MasDesignationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDesignationResponse;

import java.util.List;

public interface MasDesignationService {
    ApiResponse<List<MasDesignationResponse>> getAll(int flag);

    ApiResponse<MasDesignationResponse> getById(Long id);

    ApiResponse<MasDesignationResponse> create(MasDesignationRequest request);

    ApiResponse<MasDesignationResponse> update(Long id, MasDesignationRequest request);

    ApiResponse<MasDesignationResponse> changeStatus(Long id, String status);
}
