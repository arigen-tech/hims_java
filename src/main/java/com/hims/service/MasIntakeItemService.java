package com.hims.service;

import com.hims.request.MasIntakeItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasIntakeItemResponse;

import java.util.List;

public interface MasIntakeItemService {
    ApiResponse<List<MasIntakeItemResponse>> getAll(int flag);

    ApiResponse<MasIntakeItemResponse> getById(Long id);

    ApiResponse<MasIntakeItemResponse> create(MasIntakeItemRequest request);

    ApiResponse<MasIntakeItemResponse> update(Long id, MasIntakeItemRequest request);

    ApiResponse<MasIntakeItemResponse> changeStatus(Long id, String status);
}
