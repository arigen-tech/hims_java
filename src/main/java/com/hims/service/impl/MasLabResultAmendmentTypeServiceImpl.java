package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasLabResultAmendmentType;
import com.hims.entity.User;
import com.hims.entity.repository.MasLabResultAmendmentTypeRepository;
import com.hims.request.MasLabResultAmendmentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasLabResultAmendmentTypeResponse;
import com.hims.service.MasLabResultAmendmentTypeService;
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
public class MasLabResultAmendmentTypeServiceImpl implements MasLabResultAmendmentTypeService {

    private final MasLabResultAmendmentTypeRepository repository;

    private final AuthUtil authUtil;

    @Override
    public ApiResponse<MasLabResultAmendmentTypeResponse> create(MasLabResultAmendmentTypeRequest request) {

        try {
            log.info("create() started");

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Current User Not Found",
                        HttpStatus.NOT_FOUND.value());
            }

            MasLabResultAmendmentType entity = new MasLabResultAmendmentType();

            entity.setAmendmentTypeCode(request.getAmendmentTypeCode());
            entity.setAmendmentTypeName(request.getAmendmentTypeName());
            entity.setDescription(request.getDescription());
            entity.setStatus("y");
            entity.setCreatedBy(user.getFirstName() + " " + user.getLastName());
            entity.setLastUpdatedBy(user.getFirstName() + " " + user.getLastName());

            MasLabResultAmendmentType saved = repository.save(entity);

            log.info("create() ended");

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(saved),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("create() error", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasLabResultAmendmentTypeResponse> update(Long amendmentTypeId, MasLabResultAmendmentTypeRequest request) {

        try {
            log.info("update() started");

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Current User Not Found",
                        HttpStatus.NOT_FOUND.value());
            }

            MasLabResultAmendmentType entity =
                    repository.findById(amendmentTypeId)
                            .orElseThrow(() ->
                                    new RuntimeException("Invalid Amendment Type Id"));

            entity.setAmendmentTypeCode(request.getAmendmentTypeCode());
            entity.setAmendmentTypeName(request.getAmendmentTypeName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(
                    user.getFirstName() + " " + user.getLastName());

            MasLabResultAmendmentType saved = repository.save(entity);

            log.info("update() ended");

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(saved),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("update() error", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasLabResultAmendmentTypeResponse> changeActiveStatus(Long amendmentTypeId, String status) {

        try {
            log.info("changeActiveStatus() started");

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Current User Not Found",
                        HttpStatus.NOT_FOUND.value());
            }

            MasLabResultAmendmentType entity =
                    repository.findById(amendmentTypeId)
                            .orElseThrow(() ->
                                    new RuntimeException("Invalid Amendment Type Id"));

            entity.setStatus(status);
            entity.setLastUpdatedBy(
                    user.getFirstName() + " " + user.getLastName());

            MasLabResultAmendmentType saved = repository.save(entity);

            log.info("changeActiveStatus() ended");

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(saved),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("changeActiveStatus() error", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasLabResultAmendmentTypeResponse> getById(Long amendmentTypeId) {

        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Current User Not Found",
                        HttpStatus.NOT_FOUND.value());
            }

            MasLabResultAmendmentType entity =
                    repository.findById(amendmentTypeId)
                            .orElseThrow(() ->
                                    new RuntimeException("Invalid Amendment Type Id"));

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getById() error", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasLabResultAmendmentTypeResponse>>
    getAll(int flag) {

        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Current User Not Found",
                        HttpStatus.NOT_FOUND.value());
            }

            List<MasLabResultAmendmentType> list;

            if (flag == 0) {
                list = repository
                        .findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag == 1) {
                list = repository
                        .findByStatusIgnoreCaseOrderByAmendmentTypeNameAsc("y");
            } else {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1",
                        HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getAll() error", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasLabResultAmendmentTypeResponse mapToResponse(MasLabResultAmendmentType entity) {

        MasLabResultAmendmentTypeResponse res = new MasLabResultAmendmentTypeResponse();

        res.setAmendmentTypeId(entity.getAmendmentTypeId());
        res.setAmendmentTypeCode(entity.getAmendmentTypeCode());
        res.setAmendmentTypeName(entity.getAmendmentTypeName());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());
        res.setLastUpdateDate(entity.getLastUpdateDate());

        return res;
    }
}
