package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasPatientCondition;
import com.hims.entity.User;
import com.hims.entity.repository.MasPatientConditionRepository;
import com.hims.request.MasPatientConditionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientConditionResponse;
import com.hims.service.MasPatientConditionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasPatientConditionServiceImpl implements MasPatientConditionService {

    @Autowired
    private MasPatientConditionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasPatientConditionResponse>> getAllMasPatientCondition(int flag) {

        log.info("Fetching Patient Condition list, flag={}", flag);

        try {
            List<MasPatientCondition> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByPatientConditionNameAsc(
                    AppConstants.STATUS_Y.toLowerCase()
            )
                    : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error fetching Patient Condition list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasPatientConditionResponse> getByIdMasPatientCondition(Long id) {

        log.info("Fetching Patient Condition by id={}", id);

        try {
            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse("Patient Condition not found", 404
                    ));

        } catch (Exception e) {

            log.error("Error fetching Patient Condition by id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasPatientConditionResponse> createMasPatientCondition(
            MasPatientConditionRequest request) {

        log.info("Creating Patient Condition");

        try {
            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found",
                        404
                );
            }

            MasPatientCondition entity = new MasPatientCondition();

            entity.setPatientConditionName(request.getPatientConditionName());
            entity.setDescription(request.getDescription());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating Patient Condition", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasPatientConditionResponse> updateMasPatientCondition(
            Long id,
            MasPatientConditionRequest request) {

        log.info("Updating Patient Condition id={}", id);

        try {
            MasPatientCondition entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Patient Condition not found", 404);
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found",
                        404
                );
            }

            entity.setPatientConditionName(request.getPatientConditionName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating Patient Condition id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<MasPatientConditionResponse> changeStatusMasPatientCondition(
            Long id,
            String status) {

        log.info("Changing Patient Condition status, id={}, status={}", id, status);

        try {
            MasPatientCondition entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Patient Condition not found", 404
                );
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found",
                        404
                );
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing Patient Condition status id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private MasPatientConditionResponse mapToResponse(MasPatientCondition entity) {

        MasPatientConditionResponse res = new MasPatientConditionResponse();
        res.setPatientConditionId(entity.getPatientConditionId());
        res.setPatientConditionName(entity.getPatientConditionName());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());
        res.setLastUpdateDate(entity.getLastUpdateDate());
        res.setCreatedBy(entity.getCreatedBy());
        res.setLastUpdatedBy(entity.getLastUpdatedBy());

        return res;
    }
}