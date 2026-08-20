package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasOtTeamRole;
import com.hims.entity.User;
import com.hims.entity.repository.MasOtTeamRoleRepository;
import com.hims.request.MasOtTeamRoleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtTeamRoleResponse;
import com.hims.service.MasOtTeamRoleService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MasOtTeamRoleServiceImpl implements MasOtTeamRoleService {

    @Autowired
    private MasOtTeamRoleRepository masOtTeamRoleRepository;

    @Autowired
    private AuthUtil authUtil;

    // CREATE
    @Override
    public ApiResponse<String> saveOtTeamRole(MasOtTeamRoleRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            Optional<MasOtTeamRole> existingCode =
                    masOtTeamRoleRepository.findByRoleCodeIgnoreCase(request.getRoleCode());

            if (existingCode.isPresent()) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Role code already exists", HttpStatus.CONFLICT.value());
            }

            Optional<MasOtTeamRole> existingName =
                    masOtTeamRoleRepository.findByRoleNameIgnoreCase(request.getRoleName());

            if (existingName.isPresent()) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Role name already exists", HttpStatus.CONFLICT.value());
            }

            MasOtTeamRole entity = new MasOtTeamRole();

            entity.setRoleCode(request.getRoleCode());
            entity.setRoleName(request.getRoleName());
            entity.setDescription(request.getDescription());
            entity.setStatus(AppConstants.STATUS_Y.toUpperCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOtTeamRoleRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Team Role created successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating OT Team Role", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // GET ALL
    @Override
    public ApiResponse<List<MasOtTeamRoleResponse>> getAllOtTeamRole(int flag) {

        log.info("Fetching OT Team Role list, flag={}", flag);

        try {

            List<MasOtTeamRole> list;

            if (flag == 1) {

                list = masOtTeamRoleRepository.findByStatusIgnoreCaseOrderByRoleNameAsc(AppConstants.STATUS_Y.toLowerCase());

            } else {

                list = masOtTeamRoleRepository.findAllByOrderByStatusDescLastChgDateDesc();
            }

            List<MasOtTeamRoleResponse> response = list.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Team Role list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // GET BY ID
    @Override
    public ApiResponse<MasOtTeamRoleResponse> getById(Long id) {

        try {

            MasOtTeamRole entity = masOtTeamRoleRepository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("OT Team Role not found", HttpStatus.NOT_FOUND.value());
            }

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Team Role id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // CHANGE STATUS
    @Override
    public ApiResponse<MasOtTeamRoleResponse> changeStatus(Long id, String status) {

        try {

            MasOtTeamRole entity = masOtTeamRoleRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Team Role not found", HttpStatus.NOT_FOUND.value());
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())
                    && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status value and value should be y and n", 400);
            }

            User currentUser = authUtil.getCurrentUser();
            entity.setStatus(status.toUpperCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());
            masOtTeamRoleRepository.save(entity);

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing status for OT Team Role id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // UPDATE
    @Override
    public ApiResponse<String> updateOtTeamRole(Long id, MasOtTeamRoleRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            MasOtTeamRole entity = masOtTeamRoleRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Team Role not found", HttpStatus.NOT_FOUND.value());
            }

            Optional<MasOtTeamRole> existingCode =
                    masOtTeamRoleRepository.findByRoleCodeIgnoreCase(request.getRoleCode());

            if (existingCode.isPresent() && !existingCode.get().getOtTeamRoleId().equals(id)) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Role code already exists", HttpStatus.CONFLICT.value());
            }

            Optional<MasOtTeamRole> existingName =
                    masOtTeamRoleRepository.findByRoleNameIgnoreCase(request.getRoleName());

            if (existingName.isPresent() && !existingName.get().getOtTeamRoleId().equals(id)) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Role name already exists", HttpStatus.CONFLICT.value());
            }

            entity.setRoleCode(request.getRoleCode());
            entity.setRoleName(request.getRoleName());
            entity.setDescription(request.getDescription());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOtTeamRoleRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Team Role updated successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error updating OT Team Role id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // ENTITY -> RESPONSE
    private MasOtTeamRoleResponse toResponse(MasOtTeamRole entity) {

        MasOtTeamRoleResponse response = new MasOtTeamRoleResponse();

        response.setOtTeamRoleId(entity.getOtTeamRoleId());
        response.setRoleCode(entity.getRoleCode());
        response.setRoleName(entity.getRoleName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());

        return response;
    }
}