package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.RoleTemplateResponse;
import com.hims.response.UrlByRoleResponse;
import com.hims.service.RoleTemplateService;
import com.hims.service.UrlByRoleService;
import com.hims.service.impl.UrlByRoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/url")
public class UrlByRoleController {

    @Autowired
    private UrlByRoleService urlByRoleService;

    @GetMapping("/getAllUrlByRole/{roleId}")
    public ApiResponse getTemplatesByRoleId(@PathVariable Long roleId) {
        return urlByRoleService.getAllUrlByRoleId(roleId);
    }

    @GetMapping("/getAllUrlByRoles/{roleIds}")
    public ApiResponse getTemplatesByRoleIds(@PathVariable String roleIds) {
        List<Long> roleIdList = Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return urlByRoleService.getAllUrlByRoleIds(roleIdList);
    }

}
