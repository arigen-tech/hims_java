package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.UrlByRoleResponse;

import java.util.List;

public interface UrlByRoleService {
    ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleId(Long roleId);

    ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleIds(List<Long> roleIds);
}
