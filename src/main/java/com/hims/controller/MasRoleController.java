package com.hims.controller;

import com.hims.request.MasRoleRequest;
import com.hims.response.MasRoleResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasRoleController", description = "Controller for handling Role Master")
@RequestMapping("/roles")
public class MasRoleController {

    @Autowired
    private MasRoleService masRoleService;

    @PostMapping("/create")
    public ApiResponse<MasRoleResponse> addRole(@RequestBody MasRoleRequest request) {
        return masRoleService.addRole(request);
    }

    @PutMapping("/status/{id}")
    public ApiResponse<String> changeRoleStatus(@PathVariable String id, @RequestParam String status) {
        return masRoleService.changeRoleStatus(id, status);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<MasRoleResponse> editRole(@PathVariable String id, @RequestBody MasRoleRequest request) {
        return masRoleService.editRole(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<MasRoleResponse> getRoleById(@PathVariable String id) {
        return masRoleService.getRoleById(id);
    }

    @GetMapping("/getAllRoles/{flag}")
    public ApiResponse<List<MasRoleResponse>> getAllRoles(@PathVariable int flag) {
        return masRoleService.getAllRoles(flag);
    }
}