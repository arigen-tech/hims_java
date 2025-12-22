package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasToothCondition;
import com.hims.entity.User;
import com.hims.entity.repository.MasToothConditionRepository;
import com.hims.request.MasToothConditionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasToothConditionResponse;
import com.hims.service.MasToothConditionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MasToothConditionServiceImpl
        implements MasToothConditionService {

    @Autowired
    private MasToothConditionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasToothConditionResponse>> getAll(int flag) {
        log.info("Fetching Tooth Condition list, flag={}", flag);
        try {
            List<MasToothCondition> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByConditionNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while fetching Tooth Condition list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothConditionResponse> getById(Long id) {
        log.info("Fetching Tooth Condition by id={}", id);
        try {
            MasToothCondition condition =
                    repository.findById(id).orElse(null);

            if (condition == null) {
                log.warn("Tooth Condition not found for id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Condition ID not found!", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(condition),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while fetching Tooth Condition id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothConditionResponse> create(
            MasToothConditionRequest request) {

        log.info("Creating Tooth Condition, name={}",
                request.getConditionName());
        try {
            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }


            MasToothCondition condition = MasToothCondition.builder()
                    .conditionName(request.getConditionName())
                    .isExclusive(request.getIsExclusive())
                    .points(request.getPoints())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(condition);

            log.info("Tooth Condition created successfully, id={}",
                    condition.getConditionId());

            return ResponseUtils.createSuccessResponse(
                    toResponse(condition),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while creating Tooth Condition", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothConditionResponse> update(
            Long id, MasToothConditionRequest request) {

        log.info("Updating Tooth Condition id={}", id);
        try {
            MasToothCondition condition =
                    repository.findById(id).orElse(null);

            if (condition == null) {
                log.warn("Tooth Condition not found for update, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Condition ID not found!", 404);
            }


            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            condition.setConditionName(request.getConditionName());
            condition.setIsExclusive(request.getIsExclusive());
            condition.setPoints(request.getPoints());
            condition.setLastUpdatedBy(user.getFirstName());
            condition.setLastUpdateDate(LocalDateTime.now());

            repository.save(condition);

            log.info("Tooth Condition updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(condition),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while updating Tooth Condition id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothConditionResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Tooth Condition status, id={}, status={}", id, status);
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                log.warn("Invalid status value: {}", status);
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status!", 400
                );
            }

            MasToothCondition condition =
                    repository.findById(id).orElse(null);

            if (condition == null) {
                log.warn("Tooth Condition not found for status change, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Condition ID not found!", 404);
            }

            User user = authUtil.getCurrentUser();

            condition.setStatus(status);
            condition.setLastUpdatedBy(user.getFirstName());
            condition.setLastUpdateDate(LocalDateTime.now());

            repository.save(condition);

            log.info("Tooth Condition status updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(condition),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while updating Tooth Condition status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private MasToothConditionResponse toResponse(MasToothCondition c) {
        return new MasToothConditionResponse(
                c.getConditionId(),
                c.getConditionName(),
                c.getIsExclusive(),
                c.getPoints(),
                c.getStatus(),
                c.getLastUpdateDate()
        );
    }
}
