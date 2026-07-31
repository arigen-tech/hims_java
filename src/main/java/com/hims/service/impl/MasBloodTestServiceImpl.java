package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodCollectionType;
import com.hims.entity.MasBloodTest;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodCollectionTypeRepository;
import com.hims.entity.repository.MasBloodTestRepository;
import com.hims.request.MasBloodTestRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodTestResponse;
import com.hims.service.MasBloodTestService;
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
public class MasBloodTestServiceImpl implements MasBloodTestService {

    private final MasBloodTestRepository repository;
    private final AuthUtil authUtil;
    @Autowired
    private MasBloodCollectionTypeRepository masBloodCollectionTypeRepository;

    @Override
    public ApiResponse<List<MasBloodTestResponse>> getAll(int flag) {
        try {
            List<MasBloodTest> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByTestNameAsc("y")
                            : repository.findAllByOrderByStatusDescCreatedDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching blood test list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch blood test list", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodTestResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood test not found", 404));
        } catch (Exception e) {
            log.error("Error fetching blood test id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch record", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodTestResponse> create(MasBloodTestRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }
            Optional<MasBloodCollectionType> masBloodCollectionType=masBloodCollectionTypeRepository.findById(request.getApplicableCollectionTypeId());
            MasBloodTest entity = MasBloodTest.builder()
                    .testCode(request.getTestCode())
                    .testName(request.getTestName())
                    .isMandatory(request.getIsMandatory())
                    .applicableCollectionTypeId(masBloodCollectionType.orElse(null))
                    .status("y")
                    .createdDate(LocalDateTime.now())
                    .createdBy(user.getFirstName())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating blood test", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create blood test", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodTestResponse> update(
            Long id, MasBloodTestRequest request) {
        try {
            MasBloodTest entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood test not found", 404);
            }
            Optional<MasBloodCollectionType> masBloodCollectionType=masBloodCollectionTypeRepository.findById(request.getApplicableCollectionTypeId());
            entity.setTestCode(request.getTestCode());
            entity.setTestName(request.getTestName());
            entity.setIsMandatory(request.getIsMandatory());
            entity.setApplicableCollectionTypeId(
                    masBloodCollectionType.orElse(null));

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating blood test id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update blood test", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodTestResponse> changeStatus(
            Long id, String status) {
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            MasBloodTest entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood test not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing blood test status id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500);
        }
    }

    private MasBloodTestResponse toResponse(MasBloodTest e) {
        MasBloodTestResponse r = new MasBloodTestResponse();
        r.setBloodTestId(e.getBloodTestId());
        r.setTestCode(e.getTestCode());
        r.setTestName(e.getTestName());
        r.setIsMandatory(e.getIsMandatory());
        r.setApplicableCollectionTypeId(  e.getApplicableCollectionTypeId() != null
                ? e.getApplicableCollectionTypeId().getCollectionTypeId()
                : null);
        r.setStatus(e.getStatus());
        r.setCreatedDate(e.getCreatedDate());
        r.setCreatedBy(e.getCreatedBy());
        return r;
    }
}
