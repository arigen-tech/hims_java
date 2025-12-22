package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasToothMaster;
import com.hims.entity.User;
import com.hims.entity.repository.MasToothMasterRepository;
import com.hims.request.MasToothMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasToothMasterResponse;
import com.hims.service.MasToothMasterService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MasToothMasterServiceImpl implements MasToothMasterService {

    @Autowired
    private MasToothMasterRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasToothMasterResponse>> getAll(int flag) {
        log.info("Fetching Tooth Master list, flag={}", flag);
        try {
            List<MasToothMaster> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByDisplayOrderAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error while fetching Tooth Master list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothMasterResponse> getById(Long id) {
        log.info("Fetching Tooth Master by id={}", id);
        try {
            MasToothMaster tooth =
                    repository.findById(id).orElse(null);

            if (tooth == null) {
                log.warn("Tooth Master not found for id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Tooth ID not found!", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(tooth), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching Tooth Master id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothMasterResponse> create(
            MasToothMasterRequest request) {

        log.info("Creating Tooth Master, toothNumber={}",
                request.getToothNumber());
        try {
            User user = authUtil.getCurrentUser();

            MasToothMaster tooth = MasToothMaster.builder()
                    .toothNumber(request.getToothNumber())
                    .toothType(request.getToothType())
                    .quadrant(request.getQuadrant())
                    .displayOrder(request.getDisplayOrder())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(tooth);

            log.info("Tooth Master created successfully, id={}",
                    tooth.getToothId());

            return ResponseUtils.createSuccessResponse(
                    toResponse(tooth), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while creating Tooth Master", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothMasterResponse> update(
            Long id, MasToothMasterRequest request) {

        log.info("Updating Tooth Master id={}", id);
        try {
            MasToothMaster tooth =
                    repository.findById(id).orElse(null);

            if (tooth == null) {
                log.warn("Tooth Master not found for update, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Tooth ID not found!", 404);
            }

            User user = authUtil.getCurrentUser();

            tooth.setToothNumber(request.getToothNumber());
            tooth.setToothType(request.getToothType());
            tooth.setQuadrant(request.getQuadrant());
            tooth.setDisplayOrder(request.getDisplayOrder());
            tooth.setLastUpdatedBy(user.getFirstName());
            tooth.setLastUpdateDate(LocalDateTime.now());

            repository.save(tooth);

            log.info("Tooth Master updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(tooth), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while updating Tooth Master id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<MasToothMasterResponse> changeStatus(
            Long id, String status) {

        log.info("Changing status of Tooth Master id={}, status={}", id, status);
        try {
            MasToothMaster tooth =
                    repository.findById(id).orElse(null);

            if (tooth == null) {
                log.warn("Tooth Master not found for status change, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Tooth ID not found!", 404);
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

            tooth.setStatus(status);
            tooth.setLastUpdatedBy(user.getFirstName());
            tooth.setLastUpdateDate(LocalDateTime.now());

            repository.save(tooth);

            log.info("Status updated successfully for id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(tooth), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while updating Tooth Master status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private MasToothMasterResponse toResponse(MasToothMaster t) {
        return new MasToothMasterResponse(
                t.getToothId(),
                t.getToothNumber(),
                t.getToothType(),
                t.getQuadrant(),
                t.getDisplayOrder(),
                t.getStatus(),
                t.getLastUpdateDate()

        );
    }
}
