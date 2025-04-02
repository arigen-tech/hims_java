package com.hims.service;

import com.hims.request.RoleTemplateRequestList;
import com.hims.response.ApiResponse;
import com.hims.response.RoleTemplateResponse;

import java.util.List;

public interface RoleTemplateService {
    ApiResponse<List<RoleTemplateResponse>> addOrUpdateRoleTemplates(RoleTemplateRequestList requestList);
    ApiResponse<List<RoleTemplateResponse>> getTemplatesByRoleId(Long roleId, int flag);
}
