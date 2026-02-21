package com.hims.service;

import com.hims.request.MasBloodCollectionTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodCollectionTypeResponse;

import java.util.List;

public interface MasBloodCollectionTypeService {

    ApiResponse<List<MasBloodCollectionTypeResponse>> getAll(int flag);

    ApiResponse<MasBloodCollectionTypeResponse> getById(Long id);

    ApiResponse<MasBloodCollectionTypeResponse> create(
            MasBloodCollectionTypeRequest request);

    ApiResponse<MasBloodCollectionTypeResponse> update(
            Long id, MasBloodCollectionTypeRequest request);

    ApiResponse<MasBloodCollectionTypeResponse> changeStatus(
            Long id, String status);
}
