package com.hims.service;

import com.hims.entity.MasStoreGroup;
import com.hims.request.MasStoreGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreGroupResponse;

import java.util.List;


public interface MasStoreGroupService {
    ApiResponse<MasStoreGroupResponse> addMasStoreGroup(MasStoreGroupRequest masStoreGroupRequest);

    ApiResponse<MasStoreGroupResponse> updateMasStoreGroup(int id, MasStoreGroup masStoreGroup);

    ApiResponse<MasStoreGroupResponse> updateStatusMasStoreGroup(int id, String status);

    ApiResponse<MasStoreGroupResponse> getMasStoreGroup(int id);

    ApiResponse<List<MasStoreGroupResponse>> getMasStoreGroupAllId(int flag);
}
