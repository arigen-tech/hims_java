package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasNursingType;
import com.hims.entity.User;
import com.hims.entity.repository.MasNursingTypeRepository;
import com.hims.request.MasNursingTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasNursingTypeResponse;
import com.hims.service.MasNursingTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MasNursingTypeServiceImpl implements MasNursingTypeService {

    @Autowired
    private MasNursingTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasNursingTypeResponse>> getAll(int flag) {
        log.info("Fetching Nursing Types, flag={}", flag);

        try {
            List<MasNursingType> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByNursingTypeNameAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            log.debug("Total Nursing Types fetched: {}", list.size());

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while fetching Nursing Types", e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<MasNursingTypeResponse> getById(Long id) {
        log.info("Fetching Nursing Type by id={}", id);

        try {
            MasNursingType nursingType =
                    repository.findById(id).orElse(null);

            if (nursingType == null) {
                log.warn("Nursing Type not found for id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Nursing Type ID not found!", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(nursingType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching Nursing Type id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<MasNursingTypeResponse> create(
            MasNursingTypeRequest request) {

        log.info("Creating Nursing Type, name={}",
                request.getNursingTypeName());

        try {
            User user = authUtil.getCurrentUser();

            MasNursingType nursingType = MasNursingType.builder()
                    .nursingTypeName(request.getNursingTypeName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(nursingType);

            log.info("Nursing Type created successfully, id={}",
                    nursingType.getNursingTypeId());

            return ResponseUtils.createSuccessResponse(
                    toResponse(nursingType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while creating Nursing Type", e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasNursingTypeResponse> update(
            Long id, MasNursingTypeRequest request) {

        log.info("Updating Nursing Type id={}", id);

        try {
            MasNursingType nursingType =
                    repository.findById(id).orElse(null);

            if (nursingType == null) {
                log.warn("Nursing Type not found for update, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Nursing Type ID not found!", 404);
            }

            User user = authUtil.getCurrentUser();

            nursingType.setNursingTypeName(request.getNursingTypeName());
            nursingType.setDescription(request.getDescription());
            nursingType.setLastUpdatedBy(user.getFirstName());
            nursingType.setLastUpdateDate(LocalDateTime.now());

            repository.save(nursingType);

            log.info("Nursing Type updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(nursingType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while updating Nursing Type id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasNursingTypeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing status of Nursing Type id={}, status={}",
                id, status);

        try {
            MasNursingType nursingType =
                    repository.findById(id).orElse(null);

            if (nursingType == null) {
                log.warn("Nursing Type not found for status change, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Nursing Type ID not found!", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                log.warn("Invalid status value: {}", status);
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status!", 400
                );
            }

            User user = authUtil.getCurrentUser();

            nursingType.setStatus(status);
            nursingType.setLastUpdatedBy(user.getFirstName());
            nursingType.setLastUpdateDate(LocalDateTime.now());

            repository.save(nursingType);

            log.info("Status updated successfully for id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(nursingType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while updating status id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private MasNursingTypeResponse toResponse(MasNursingType m) {
        return new MasNursingTypeResponse(
                m.getNursingTypeId(),
                m.getNursingTypeName(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy(),
                m.getDescription()
        );
    }
}
