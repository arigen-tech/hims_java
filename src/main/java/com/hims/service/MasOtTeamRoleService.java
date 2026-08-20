package com.hims.service;

import com.hims.request.MasOtTeamRoleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtTeamRoleResponse;

import java.util.List;

public interface MasOtTeamRoleService {

    ApiResponse<String> saveOtTeamRole(MasOtTeamRoleRequest request);

    ApiResponse<List<MasOtTeamRoleResponse>> getAllOtTeamRole(int flag);

    ApiResponse<MasOtTeamRoleResponse> getById(Long id);

    ApiResponse<MasOtTeamRoleResponse> changeStatus(Long id, String status);

    ApiResponse<String> updateOtTeamRole(Long id, MasOtTeamRoleRequest request);
}