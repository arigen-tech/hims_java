package com.hims.service;

import com.hims.entity.MasHSN;
import com.hims.request.MasHsnRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasHsnResponse;

import java.util.List;

public interface MasHsnService {
    ApiResponse<List<MasHsnResponse>> getAllMasStoreItem(int flag);

    ApiResponse<MasHsnResponse> addMasHSN(MasHsnRequest masHsnRequest);

    ApiResponse<MasHsnResponse> findById(String id);

    ApiResponse<MasHsnResponse> changeMasHsnStatus(String id, String status);

    ApiResponse<MasHsnResponse> update(String id, MasHsnRequest request);
}
