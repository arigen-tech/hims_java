package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRole;
import com.hims.entity.repository.MasRoleRepository;
import com.hims.request.MasRoleRequest;
import com.hims.response.MasRoleResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasRoleService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasRoleServiceImpl implements MasRoleService {

    @Autowired
    private MasRoleRepository masRoleRepository;

    @Override
    public ApiResponse<MasRoleResponse> addRole(MasRoleRequest request) {
        MasRole role = new MasRole();
        role.setId(java.util.UUID.randomUUID().toString());
        role.setRoleCode(request.getRoleCode());
        role.setRoleDesc(request.getRoleDesc());
        role.setIsactive(request.getIsActive());
        role.setCreatedOn(Instant.now());
        role.setUpdatedOn(Instant.now());

        MasRole savedRole = masRoleRepository.save(role);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedRole), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeRoleStatus(String id, Boolean isActive) {
        Optional<MasRole> roleOpt = masRoleRepository.findById(id);
        if (roleOpt.isPresent()) {
            MasRole role = roleOpt.get();
            role.setIsactive(isActive);
            role.setUpdatedOn(Instant.now());
            masRoleRepository.save(role);
            return ResponseUtils.createSuccessResponse("Role status updated", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Role not found", 404);
        }
    }

    @Override
    public ApiResponse<MasRoleResponse> editRole(String id, MasRoleRequest request) {
        Optional<MasRole> roleOpt = masRoleRepository.findById(id);
        if (roleOpt.isPresent()) {
            MasRole role = roleOpt.get();
            role.setRoleCode(request.getRoleCode());
            role.setRoleDesc(request.getRoleDesc());
            role.setIsactive(request.getIsActive());
            role.setUpdatedOn(Instant.now());

            masRoleRepository.save(role);
            return ResponseUtils.createSuccessResponse(mapToResponse(role), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Role not found", 404);
        }
    }

    @Override
    public ApiResponse<MasRoleResponse> getRoleById(String id) {
        return masRoleRepository.findById(id)
                .map(role -> ResponseUtils.createSuccessResponse(mapToResponse(role), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Role not found", 404));
    }

    @Override
    public ApiResponse<List<MasRoleResponse>> getAllRoles() {
        List<MasRoleResponse> roles = masRoleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(roles, new TypeReference<>() {});
    }

    private MasRoleResponse mapToResponse(MasRole role) {
        MasRoleResponse response = new MasRoleResponse();
        response.setId(role.getId());
        response.setRoleCode(role.getRoleCode());
        response.setRoleDesc(role.getRoleDesc());
        response.setIsActive(role.getIsactive());
        response.setCreatedOn(role.getCreatedOn());
        response.setUpdatedOn(role.getUpdatedOn());
        return response;
    }
}