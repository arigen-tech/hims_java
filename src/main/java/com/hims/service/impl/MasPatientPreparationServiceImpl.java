package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasPatientPreparation;
import com.hims.entity.User;
import com.hims.entity.repository.MasPatientPreparationRepo;
import com.hims.request.MasPatientPreparationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientPreparationResponse;
import com.hims.service.MasPatientPreparationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasPatientPreparationServiceImpl implements MasPatientPreparationService {

    private final MasPatientPreparationRepo repo;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<MasPatientPreparationResponse> create(MasPatientPreparationRequest request) {
        try {
            log.info("create() started");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            if (repo.existsByPreparationCodeIgnoreCase(request.getPreparationCode())) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Preparation Code already exists",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            MasPatientPreparation entity = getEntity(request, currentUser);

            MasPatientPreparation saved = repo.save(entity);
            log.info("create() ended");

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("create() error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static MasPatientPreparation getEntity(MasPatientPreparationRequest request, User currentUser) {
        MasPatientPreparation entity = new MasPatientPreparation();
        entity.setPreparationCode(request.getPreparationCode());
        entity.setPreparationName(request.getPreparationName());
        entity.setInstructions(request.getInstructions());
        entity.setApplicableTo(request.getApplicableTo());
        entity.setStatus("y");
        entity.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
        entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
        return entity;
    }

    @Override
    public ApiResponse<MasPatientPreparationResponse> update(Long preparationId, MasPatientPreparationRequest request) {
        try {
            log.info("update() started");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasPatientPreparation entity = repo.findById(preparationId)
                    .orElseThrow(() -> new RuntimeException("Invalid Preparation Id"));

            entity.setPreparationCode(request.getPreparationCode());
            entity.setPreparationName(request.getPreparationName());
            entity.setInstructions(request.getInstructions());
            entity.setApplicableTo(request.getApplicableTo());
            entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());

            MasPatientPreparation saved = repo.save(entity);
            log.info("update() ended");

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("update() error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasPatientPreparationResponse> changeActiveStatus(Long preparationId, String status) {
        try {
            log.info("changeActiveStatus() started");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasPatientPreparation entity = repo.findById(preparationId)
                    .orElseThrow(() -> new RuntimeException("Invalid Preparation Id"));

            entity.setStatus(status);
            entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());

            MasPatientPreparation saved = repo.save(entity);
            log.info("changeActiveStatus() ended");

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("changeActiveStatus() error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasPatientPreparationResponse> getById(Long preparationId) {
        try {
            log.info("getById() started");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasPatientPreparation entity = repo.findById(preparationId)
                    .orElseThrow(() -> new RuntimeException("Invalid Preparation Id"));

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getById() error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasPatientPreparationResponse>> getAll(int flag) {
        try {
            log.info("getAll() started");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            List<MasPatientPreparation> list;
            if (flag == 0) {
                list = repo.findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag == 1) {
                list = repo.findByStatusIgnoreCaseOrderByPreparationNameAsc("y");
            } else {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("getAll() error", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasPatientPreparationResponse mapToResponse(MasPatientPreparation entity) {
        MasPatientPreparationResponse res = new MasPatientPreparationResponse();
        res.setPreparationId(entity.getPreparationId());
        res.setPreparationCode(entity.getPreparationCode());
        res.setPreparationName(entity.getPreparationName());
        res.setInstructions(entity.getInstructions());
        res.setApplicableTo(entity.getApplicableTo());
        res.setStatus(entity.getStatus());
        res.setLastUpdateDate(entity.getLastUpdateDate());
        return res;
    }
}
