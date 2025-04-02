package com.hims.controller;

import com.hims.request.RoleTemplateRequestList;
import com.hims.response.ApiResponse;
import com.hims.response.RoleTemplateResponse;
import com.hims.service.RoleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role-template")
public class RoleTemplateController {

    @Autowired
    private RoleTemplateService roleTemplateService;

    @PostMapping("/assignTemplates")
    public ApiResponse<List<RoleTemplateResponse>> addOrUpdateRoleTemplates(@RequestBody RoleTemplateRequestList requestList) {
        return roleTemplateService.addOrUpdateRoleTemplates(requestList);
    }

    @GetMapping("/getAllAssignedTemplates/{roleId}/{flag}")
    public ApiResponse<List<RoleTemplateResponse>> getTemplatesByRoleId(
            @PathVariable Long roleId,
            @PathVariable int flag) {
        return roleTemplateService.getTemplatesByRoleId(roleId, flag);
    }
}
