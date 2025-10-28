package com.hims.service;

import com.hims.request.DgMasCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasCollectionResponse;

import java.util.List;

public interface DgMasCollectionService {
    ApiResponse<DgMasCollectionResponse> addDgMasCollection(DgMasCollectionRequest dgMasCollectionRequest);

    ApiResponse<DgMasCollectionResponse> update(Long id, DgMasCollectionRequest request);

    ApiResponse<List<DgMasCollectionResponse>> getDgMasCollection(int flag);

    ApiResponse<DgMasCollectionResponse> findById(Long id);

    ApiResponse<DgMasCollectionResponse> changeDgMasCollectionStatus(Long id, String status);
}
