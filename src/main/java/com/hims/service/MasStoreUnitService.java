package com.hims.service;

import com.hims.request.MasStoreUnitRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreUnitResponse;

import java.util.List;

public interface MasStoreUnitService {
    ApiResponse<List<MasStoreUnitResponse>> getAllUnits(int flag);
    ApiResponse<MasStoreUnitResponse> findByUnit(Long unit_id);
    public ApiResponse<MasStoreUnitResponse> addUnit(MasStoreUnitRequest masStoreUnitRequest);
    public ApiResponse<MasStoreUnitResponse> updateUnit(Long unit_id, MasStoreUnitRequest masStoreUnitRequest);
    ApiResponse<MasStoreUnitResponse> changeStat(Long unit_id, String stat);
}
