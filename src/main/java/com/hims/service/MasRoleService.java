package com.hims.service;

import com.hims.request.MasRoleRequest;
import com.hims.response.MasRoleResponse;
import com.hims.response.ApiResponse;
import java.util.List;

public interface MasRoleService {
    ApiResponse<MasRoleResponse> addRole(MasRoleRequest request);
    ApiResponse<String> changeRoleStatus(Long id, String status);
    ApiResponse<MasRoleResponse> editRole(Long id, MasRoleRequest request);
    ApiResponse<MasRoleResponse> getRoleById(Long id);
    ApiResponse<List<MasRoleResponse>> getAllRoles(int flag);
}