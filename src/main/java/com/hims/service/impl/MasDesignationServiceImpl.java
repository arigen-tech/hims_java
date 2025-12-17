package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDesignation;
import com.hims.entity.MasUserType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDesignationRepository;
import com.hims.entity.repository.MasUserTypeRepository;
import com.hims.request.MasDesignationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDesignationResponse;
import com.hims.service.MasDesignationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasDesignationServiceImpl  implements MasDesignationService {


    @Autowired
    private MasDesignationRepository designationRepo;

    @Autowired
    private MasUserTypeRepository userTypeRepo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasDesignationResponse>> getAll(int flag) {
        try {
            List<MasDesignation> list =
                    (flag == 1)
                            ? designationRepo
                            .findByStatusIgnoreCaseOrderByDesignationNameAsc("y")
                            : designationRepo
                            .findAllByOrderByLastUpdateDateDesc();

            List<MasDesignationResponse> response =
                    list.stream().map(this::toResponse).toList();

            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<List<MasDesignationResponse>> getById(Long id) {
        try {
            List<MasDesignation> designations = designationRepo.findByUserTypeIdUserTypeIdAndStatus(id, "y");

            if (designations.isEmpty())
                return ResponseUtils.createNotFoundResponse(
                        "No designations found for this user type", 404);

            List<MasDesignationResponse> responseList = designations.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(
                    responseList, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDesignationResponse> create(
            MasDesignationRequest request) {
        try {
            MasUserType userType =
                    userTypeRepo.findById(request.getUserTypeId())
                            .orElse(null);

            if (userType == null)
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid User Type", 400);

            User user = authUtil.getCurrentUser();

            MasDesignation designation = MasDesignation.builder()
                    .designationName(request.getDesignationName())
                    .userTypeId(userType)
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            designationRepo.save(designation);

            return ResponseUtils.createSuccessResponse(
                    toResponse(designation), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDesignationResponse> update(
            Long id, MasDesignationRequest request) {
        try {
            MasDesignation designation =
                    designationRepo.findById(id).orElse(null);

            if (designation == null)
                return ResponseUtils.createNotFoundResponse(
                        "Designation not found", 404);

            MasUserType userType =
                    userTypeRepo.findById(request.getUserTypeId())
                            .orElse(null);

            if (userType == null)
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid User Type", 400);

            User user = authUtil.getCurrentUser();

            designation.setDesignationName(request.getDesignationName());
            designation.setUserTypeId(userType);
            designation.setLastUpdatedBy(user.getFirstName());
            designation.setLastUpdateDate(LocalDateTime.now());

            designationRepo.save(designation);

            return ResponseUtils.createSuccessResponse(
                    toResponse(designation), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDesignationResponse> changeStatus(
            Long id, String status) {
        try {
            MasDesignation designation =
                    designationRepo.findById(id).orElse(null);

            if (designation == null)
                return ResponseUtils.createNotFoundResponse(
                        "Designation not found", 404);

            if (!status.equalsIgnoreCase("y")
                    && !status.equalsIgnoreCase("n"))
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);

            User user = authUtil.getCurrentUser();

            designation.setStatus(status);
            designation.setLastUpdatedBy(user.getFirstName());
            designation.setLastUpdateDate(LocalDateTime.now());

            designationRepo.save(designation);

            return ResponseUtils.createSuccessResponse(
                    toResponse(designation), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    private MasDesignationResponse toResponse(MasDesignation d) {
        return new MasDesignationResponse(
                d.getDesignationId(),
                d.getDesignationName(),
                d.getUserTypeId().getUserTypeId(),
                d.getUserTypeId().getUserTypeName(),
                d.getStatus(),
                d.getCreatedBy(),
                d.getLastUpdatedBy(),
                d.getLastUpdateDate()
        );
    }
}
