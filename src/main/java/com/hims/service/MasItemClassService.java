package com.hims.service;

import com.hims.request.MasItemClassRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemClassResponse;

import java.util.List;

public interface MasItemClassService {

    ApiResponse<MasItemClassResponse> addMasItemClass(MasItemClassRequest masStoreSectionRequest);

    ApiResponse<List<MasItemClassResponse>> getAllMasItemClass(int flag);

    ApiResponse<MasItemClassResponse> findById(Integer id);

    ApiResponse<MasItemClassResponse> changeMasItemClassStatus(int id, String status);

    ApiResponse<MasItemClassResponse> updateMasItemClass(int id, MasItemClassRequest masItemClassdRequest);
}
