package com.hims.service;

import com.hims.request.MasRoleRequest;
import com.hims.response.MasRoleResponse;
import com.hims.response.ApiResponse;
import java.util.List;

public interface MasRoleService {
    ApiResponse<MasRoleResponse> addRole(MasRoleRequest request);
    ApiResponse<String> changeRoleStatus(String id, String status);
    ApiResponse<MasRoleResponse> editRole(String id, MasRoleRequest request);
    ApiResponse<MasRoleResponse> getRoleById(String id);
    ApiResponse<List<MasRoleResponse>> getAllRoles(int flag);
}