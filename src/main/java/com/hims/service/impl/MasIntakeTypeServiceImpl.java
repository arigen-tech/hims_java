package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIntakeType;
import com.hims.entity.User;
import com.hims.entity.repository.MasIntakeTypeRepository;
import com.hims.request.MasIntakeTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasIntakeTypeResponse;
import com.hims.response.UserContext;
import com.hims.service.MasIntakeTypeService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasIntakeTypeServiceImpl  implements MasIntakeTypeService {
    @Autowired
    private MasIntakeTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasIntakeTypeResponse>> getAll(int flag) {
        try {
            List<MasIntakeType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByIntakeTypeNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasIntakeTypeResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasIntakeTypeResponse> getById(Long id) {
        try {
            MasIntakeType intake = repository.findById(id).orElse(null);

            if (intake == null)
                return ResponseUtils.createNotFoundResponse("Intake Type ID not found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(intake), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasIntakeTypeResponse> create(MasIntakeTypeRequest request) {
        try {
            UserContext userContext = userContextService.getCurrentUserContext();

            MasIntakeType intake = MasIntakeType.builder()
                    .intakeTypeName(request.getIntakeTypeName())
                    .isLiquid(request.getIsLiquid())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasIntakeType saved = repository.save(intake);

            return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasIntakeTypeResponse> update(Long id, MasIntakeTypeRequest request) {
        try {
            MasIntakeType intake = repository.findById(id).orElse(null);

            if (intake == null)
                return ResponseUtils.createNotFoundResponse("Intake Type ID not found!", 404);

            UserContext userContext = userContextService.getCurrentUserContext();

            intake.setIntakeTypeName(request.getIntakeTypeName());
            intake.setIsLiquid(request.getIsLiquid());
            intake.setLastUpdatedBy(userContext.getUserFullName());
            intake.setLastUpdateDate(LocalDateTime.now());

            repository.save(intake);

            return ResponseUtils.createSuccessResponse(toResponse(intake), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Update failed: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasIntakeTypeResponse> changeStatus(Long id, String status) {
        try {
            MasIntakeType intake = repository.findById(id).orElse(null);

            if (intake == null)
                return ResponseUtils.createNotFoundResponse("Intake Type ID not found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status!",
                        400
                );

            UserContext userContext = userContextService.getCurrentUserContext();

            intake.setStatus(status);
            intake.setLastUpdatedBy(userContext.getUserFullName());
            intake.setLastUpdateDate(LocalDateTime.now());

            repository.save(intake);

            return ResponseUtils.createSuccessResponse(toResponse(intake), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Status update failed: " + e.getMessage(),
                    500
            );
        }
    }

    private MasIntakeTypeResponse toResponse(MasIntakeType m) {
        return new MasIntakeTypeResponse(
                m.getIntakeTypeId(),
                m.getIntakeTypeName(),
                m.getIsLiquid(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy()
        );
    }
}
