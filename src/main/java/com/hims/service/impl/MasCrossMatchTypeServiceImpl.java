package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasCrossMatchType;
import com.hims.entity.User;
import com.hims.entity.repository.MasCrossMatchTypeRepository;
import com.hims.request.MasCrossMatchTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCrossMatchTypeResponse;
import com.hims.service.MasCrossMatchTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasCrossMatchTypeServiceImpl implements MasCrossMatchTypeService {
    @Autowired
    private  MasCrossMatchTypeRepository repository;
    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasCrossMatchTypeResponse>> getAll(int flag) {
        try {
            List<MasCrossMatchType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByCrossMatchNameAsc("y")
                            : repository.findAllByOrderByStatusDescCreatedDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching crossmatch type list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to fetch crossmatch type list",
                    500);
        }
    }

    @Override
    public ApiResponse<MasCrossMatchTypeResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            toResponse(entity), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Crossmatch type not found", 404));
        } catch (Exception e) {
            log.error("Error fetching crossmatch type by id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to fetch record",
                    500);
        }
    }

    @Override
    public ApiResponse<MasCrossMatchTypeResponse> create(MasCrossMatchTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        401);
            }



            MasCrossMatchType entity = new MasCrossMatchType();
            entity.setCrossMatchCode(request.getCrossMatchCode());
            entity.setCrossMatchName(request.getCrossMatchName());
            entity.setDescription(request.getDescription());
            entity.setTurnaroundTimeMin(request.getTurnaroundTimeMin());
            entity.setChargeAmount(request.getChargeAmount());
            entity.setIsEmergencyAllowed(request.getIsEmergencyAllowed().toLowerCase());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedDate(LocalDateTime.now());
            entity.setCreatedBy(user.getFullName());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating crossmatch type", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to create crossmatch type",
                    500);
        }
    }

    @Override
    public ApiResponse<MasCrossMatchTypeResponse> update(Long id, MasCrossMatchTypeRequest request) {
        try {
            MasCrossMatchType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "CrossMatch type not found", 404);
            }
            entity.setCrossMatchCode(request.getCrossMatchCode());
            entity.setCrossMatchName(request.getCrossMatchName());
            entity.setDescription(request.getDescription());
            entity.setTurnaroundTimeMin(request.getTurnaroundTimeMin());
            entity.setChargeAmount(request.getChargeAmount());
            entity.setIsEmergencyAllowed(request.getIsEmergencyAllowed().toUpperCase());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating crossMatch type id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to update crossMatch type",
                    500);
        }
    }

    @Override
    public ApiResponse<MasCrossMatchTypeResponse> changeStatus(Long id, String status) {
        try {
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status",
                        400);
            }

            MasCrossMatchType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "CrossMatch type not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing crossMatch type status id : {}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Failed to change status",
                    500);
        }
    }

    private MasCrossMatchTypeResponse toResponse(MasCrossMatchType entity) {
        MasCrossMatchTypeResponse response = new MasCrossMatchTypeResponse();
        response.setId(entity.getId());
        response.setCrossMatchCode(entity.getCrossMatchCode());
        response.setCrossMatchName(entity.getCrossMatchName());
        response.setDescription(entity.getDescription());
        response.setTurnaroundTimeMin(entity.getTurnaroundTimeMin());
        response.setChargeAmount(entity.getChargeAmount());
        response.setIsEmergencyAllowed(entity.getIsEmergencyAllowed());
        response.setStatus(entity.getStatus());
        response.setCreatedDate(entity.getCreatedDate());
        response.setCreatedBy(entity.getCreatedBy());
        return response;
    }




}
