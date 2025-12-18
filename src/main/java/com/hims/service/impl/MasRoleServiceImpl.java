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

    private boolean isValidStatus(String status) {
        return "y".equalsIgnoreCase(status) || "n".equalsIgnoreCase(status);
    }

    @Override
    public ApiResponse<MasRoleResponse> addRole(MasRoleRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Use 'Y' or 'N'.", 400);
        }

        MasRole role = new MasRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleDesc(request.getRoleDesc());
        role.setStatus(request.getStatus().toLowerCase());
        role.setCreatedOn(Instant.now());
        role.setUpdatedOn(Instant.now());

        MasRole savedRole = masRoleRepository.save(role);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedRole), new TypeReference<>() {});
    }

    public ApiResponse<String> changeRoleStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Use 'Y' or 'N'.", 400);
        }

        Optional<MasRole> roleOpt = masRoleRepository.findById(id);
        if (roleOpt.isPresent()) {
            MasRole role = roleOpt.get();
            role.setStatus(status.toLowerCase());
            role.setUpdatedOn(Instant.now());
            masRoleRepository.save(role);
            return ResponseUtils.createSuccessResponse("Role status updated", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Role not found", 404);
        }
    }

    public ApiResponse<MasRoleResponse> editRole(Long id, MasRoleRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Use 'Y' or 'N'.", 400);
        }

        Optional<MasRole> roleOpt = masRoleRepository.findById(id);
        if (roleOpt.isPresent()) {
            MasRole role = roleOpt.get();
            role.setRoleCode(request.getRoleCode());
            role.setRoleDesc(request.getRoleDesc());
            role.setStatus(request.getStatus().toLowerCase());
            role.setUpdatedOn(Instant.now());

            masRoleRepository.save(role);
            return ResponseUtils.createSuccessResponse(mapToResponse(role), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Role not found", 404);
        }
    }

    public ApiResponse<MasRoleResponse> getRoleById(Long id) {
        return masRoleRepository.findById(id)
                .map(role -> ResponseUtils.createSuccessResponse(mapToResponse(role), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Role not found", 404));
    }

    @Override
    public ApiResponse<List<MasRoleResponse>> getAllRoles(int flag) {
        List<MasRole> roles;

        if (flag == 1) {
            roles = masRoleRepository.findByStatusIgnoreCase("y");
        } else if (flag == 0) {
            roles = masRoleRepository.findAllByOrderByStatusDescUpdatedOnDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasRoleResponse> responses = roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasRoleResponse mapToResponse(MasRole role) {
        MasRoleResponse response = new MasRoleResponse();
        response.setId(role.getId());
        response.setRoleCode(role.getRoleCode());
        response.setRoleDesc(role.getRoleDesc());
        response.setStatus(role.getStatus());
        response.setCreatedOn(role.getCreatedOn());
        response.setUpdatedOn(role.getUpdatedOn());
        return response;
    }
}
