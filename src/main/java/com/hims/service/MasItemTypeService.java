package com.hims.service;

import com.hims.entity.MasItemType;
import com.hims.request.MasItemTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemTypeResponse;

import java.util.List;

public interface MasItemTypeService {
    ApiResponse<MasItemTypeResponse> addMasItemType(MasItemTypeRequest masItemTypeRequest);

    ApiResponse<MasItemTypeResponse> updateMasItemTypeID(int id, MasItemTypeRequest masItemTypeRequest);

    ApiResponse<MasItemTypeResponse> updateMasItemTypeStatus(int id, String status);

    ApiResponse<MasItemTypeResponse> getByMasItemTypeStatus(int id);

    ApiResponse<List<MasItemTypeResponse>> getAllMasItemTypeStatus(int flag);

    ApiResponse<List<MasItemTypeResponse>> findItemType(Long id);
}
