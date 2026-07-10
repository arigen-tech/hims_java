package com.hims.service;

import com.hims.request.MasBloodComponentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodComponentResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface MasBloodComponentService {
    ApiResponse<List<MasBloodComponentResponse>> getAll(int flag);

    ApiResponse<MasBloodComponentResponse> getById(Long id);

    ApiResponse<MasBloodComponentResponse> create(@Valid MasBloodComponentRequest request);

    ApiResponse<MasBloodComponentResponse> update(Long id, @Valid MasBloodComponentRequest request);

    ApiResponse<MasBloodComponentResponse> changeStatus(Long id, String status);
}
