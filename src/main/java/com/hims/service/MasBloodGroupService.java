package com.hims.service;

import com.hims.request.MasBloodGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodGroupResponse;

import java.util.List;

public interface MasBloodGroupService {
    ApiResponse<List<MasBloodGroupResponse>> getAllBloodGroups(int flag);
    ApiResponse<MasBloodGroupResponse> addBloodGroup(MasBloodGroupRequest bloodGroupRequest);
    ApiResponse<MasBloodGroupResponse> updateBloodGroup(Long id, MasBloodGroupRequest bloodGroupRequest);
    public ApiResponse<MasBloodGroupResponse> changeBloodGroupStatus(Long id, String status);
    public ApiResponse<MasBloodGroupResponse> findById(Long id);
}
