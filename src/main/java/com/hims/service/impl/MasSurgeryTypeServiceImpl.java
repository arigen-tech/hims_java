package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasSurgery;
import com.hims.entity.MasSurgeryType;
import com.hims.entity.User;
import com.hims.entity.repository.MasSurgeryRepository;
import com.hims.entity.repository.MasSurgeryTypeRepository;
import com.hims.request.MasSurgeryTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryResponse;
import com.hims.response.MasSurgeryTypeResponse;
import com.hims.service.MasSurgeryTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MasSurgeryTypeServiceImpl implements MasSurgeryTypeService {

    private final MasSurgeryTypeRepository repository;
    private final MasSurgeryRepository masSurgeryRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasSurgeryTypeResponse>> getAllMasSurgeryType(int flag) {
        try {
            List<MasSurgeryType> list;
            if (flag == 1) {
                list = repository.findActive(AppConstants.STATUS_Y);
            } else if (flag == 0) {
                list = repository.findActiveAndDeactive();
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value", HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching surgery type list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<MasSurgeryTypeResponse> getByIdMasSurgeryType(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Surgery type not found", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        } catch (Exception e) {
            log.error("mas surgery type error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryTypeResponse> createMasSurgeryType(MasSurgeryTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasSurgeryType entity = MasSurgeryType.builder()
                    .surgeryTypeCode(request.getSurgeryTypeCode())
                    .surgeryTypeName(request.getSurgeryTypeName())
                    .description(request.getDescription())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(user.getFullName())
                    .lastUpdatedBy(user.getFullName())
                    .lastUpdatedDate(LocalDateTime.now())
                    .build();

            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error creating surgery type", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Creation failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryTypeResponse> updateMasSurgeryType(Long id, MasSurgeryTypeRequest request) {
        try {
            MasSurgeryType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Surgery type not found", 404);
            }
            User user = authUtil.getCurrentUser();

            entity.setSurgeryTypeCode(request.getSurgeryTypeCode());
            entity.setSurgeryTypeName(request.getSurgeryTypeName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("mas surgery type error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Update failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryTypeResponse> changeStatusMasSurgeryType(Long id, String status) {
        try {
            MasSurgeryType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Surgery type not found", 404);
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase()) && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status", HttpStatus.BAD_REQUEST.value());
            }
            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("mas surgery type error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status update failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasSurgeryResponse>> masSurgeryBySurgeryType(Long surgeryTypeId) {
        List<MasSurgery> masSurgeries = masSurgeryRepository.findBySurgeryType_SurgeryTypeIdAndStatusIgnoreCase(
                        surgeryTypeId, AppConstants.STATUS_Y);

        List<MasSurgeryResponse> response = masSurgeries.stream()
                .map(surgery -> {
                    MasSurgeryResponse dto = new MasSurgeryResponse();

                    dto.setSurgeryId(surgery.getSurgeryId());dto.setSurgeryCode(surgery.getSurgeryCode());
                    dto.setSurgeryName(surgery.getSurgeryName());
                    if (surgery.getSurgeryType() != null) {
                        dto.setSurgeryTypeId(surgery.getSurgeryType().getSurgeryTypeId());
                        dto.setSurgeryTypeName(surgery.getSurgeryType().getSurgeryTypeName());
                    }
                    dto.setSurgeryLevel(surgery.getSurgeryLevel());
                    dto.setIsAnesthesiaRequired(surgery.getIsAnesthesiaRequired());
                    dto.setIsAdmissionRequired(surgery.getIsAdmissionRequired());
                    dto.setIsImplantRequired(surgery.getIsImplantRequired());
                    dto.setStatus(surgery.getStatus());
                    return dto;
                })
                .toList();

        return ResponseUtils.createSuccessResponse(response,new TypeReference<>(){});

    }

    private MasSurgeryTypeResponse toResponse(MasSurgeryType e) {
        MasSurgeryTypeResponse res = new MasSurgeryTypeResponse();
        res.setSurgeryTypeId(e.getSurgeryTypeId());
        res.setSurgeryTypeCode(e.getSurgeryTypeCode());
        res.setSurgeryTypeName(e.getSurgeryTypeName());
        res.setDescription(e.getDescription());
        res.setStatus(e.getStatus());
        return res;
    }
}