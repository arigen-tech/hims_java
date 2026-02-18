package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodCompatibility;
import com.hims.entity.MasBloodComponent;
import com.hims.entity.MasBloodGroup;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodCompatibilityRepository;
import com.hims.entity.repository.MasBloodComponentRepository;
import com.hims.entity.repository.MasBloodGroupRepository;
import com.hims.request.MasBloodCompatibilityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodCompatibilityResponse;
import com.hims.service.MasBloodCompatibilityService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasBloodCompatibilityServiceImpl implements MasBloodCompatibilityService {

    private final MasBloodCompatibilityRepository repository;
    private final AuthUtil authUtil;
    @Autowired
    private MasBloodComponentRepository masBloodComponentRepository;
    @Autowired
    private MasBloodGroupRepository masBloodGroupRepository;

    @Override
    public ApiResponse<List<MasBloodCompatibilityResponse>> getAll(int flag) {
        try {
            List<MasBloodCompatibility> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByCompatibilityIdAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching blood compatibility list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch blood compatibility list", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCompatibilityResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood compatibility not found", 404));
        } catch (Exception e) {
            log.error("Error fetching blood compatibility id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch record", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCompatibilityResponse> create(
            MasBloodCompatibilityRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }
            Optional<MasBloodComponent> masBloodComponent=masBloodComponentRepository.findById(request.getComponentId());
                    Optional<MasBloodGroup> masBloodGroup=masBloodGroupRepository.findById(request.getDonorBloodGroupId());
            Optional<MasBloodGroup> masBloodGroup1=masBloodGroupRepository.findById(request.getPatientBloodGroupId());


            MasBloodCompatibility entity = MasBloodCompatibility.builder()
                    .componentId(masBloodComponent.orElse(null))
                    .patientBloodGroupId(masBloodGroup1.orElse(null))
                    .donorBloodGroupId(masBloodGroup.orElse(null))
                    .isPreferred(request.getIsPreferred())
                    .status("y")
                    .lastUpdateDate(LocalDateTime.now())
                    .createdBy(user.getFirstName())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating blood compatibility", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create blood compatibility", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCompatibilityResponse> update(
            Long id, MasBloodCompatibilityRequest request) {
        try {
            MasBloodCompatibility entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood compatibility not found", 404);
            }

            User user = authUtil.getCurrentUser();
            Optional<MasBloodComponent> masBloodComponent=masBloodComponentRepository.findById(request.getComponentId());
            Optional<MasBloodGroup> masBloodGroup=masBloodGroupRepository.findById(request.getDonorBloodGroupId());
            Optional<MasBloodGroup> masBloodGroup1=masBloodGroupRepository.findById(request.getPatientBloodGroupId());


            entity.setComponentId(masBloodComponent.orElse(null));
            entity.setPatientBloodGroupId(masBloodGroup1.orElse(null));
            entity.setDonorBloodGroupId(masBloodGroup.orElse(null));
            entity.setIsPreferred(request.getIsPreferred());
            entity.setLastUpdateDate(LocalDateTime.now());
            entity.setLastUpdatedBy(user.getFirstName());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating blood compatibility id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update blood compatibility", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCompatibilityResponse> changeStatus(
            Long id, String status) {
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            MasBloodCompatibility entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood compatibility not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing blood compatibility status id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500);
        }
    }

    private MasBloodCompatibilityResponse toResponse(MasBloodCompatibility e) {
        MasBloodCompatibilityResponse r = new MasBloodCompatibilityResponse();
        r.setCompatibilityId(e.getCompatibilityId());
        r.setComponentId(e.getComponentId()!=null?e.getComponentId().getComponentId():null);
        r.setComponentName(e.getComponentId()!=null?e.getComponentId().getComponentName():null);
        r.setPatientBloodGroupId(e.getPatientBloodGroupId()!=null?e.getPatientBloodGroupId().getBloodGroupId():null);
        r.setPatientBloodGroup(e.getPatientBloodGroupId()!=null?e.getPatientBloodGroupId().getBloodGroupCode():null);
        r.setDonorBloodGroupId(e.getDonorBloodGroupId()!=null?e.getDonorBloodGroupId().getBloodGroupId():null);
        r.setDonorBloodGroup(e.getDonorBloodGroupId()!=null?e.getDonorBloodGroupId().getBloodGroupCode():null);
        r.setIsPreferred(e.getIsPreferred());
        r.setStatus(e.getStatus());
        r.setLastUpdateDate(e.getLastUpdateDate());
        r.setCreatedBy(e.getCreatedBy());
        r.setLastUpdatedBy(e.getLastUpdatedBy());
        return r;
    }
}
