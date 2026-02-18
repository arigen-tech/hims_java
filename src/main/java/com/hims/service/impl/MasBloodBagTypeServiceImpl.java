package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodBagType;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodBagTypeRepository;
import com.hims.request.MasBloodBagTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodBagTypeResponse;
import com.hims.service.MasBloodBagTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasBloodBagTypeServiceImpl implements MasBloodBagTypeService {

    private final MasBloodBagTypeRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasBloodBagTypeResponse>> getAll(int flag) {
        log.info("Fetching Blood Bag Type list, flag={}", flag);
        try {
            List<MasBloodBagType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByBagTypeNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Blood Bag Type list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodBagTypeResponse> getById(Long id) {
        log.info("Fetching Blood Bag Type by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood Bag Type not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Blood Bag Type by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodBagTypeResponse> create(
            MasBloodBagTypeRequest request) {

        log.info("Creating Blood Bag Type");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasBloodBagType entity = MasBloodBagType.builder()
                    .bagTypeCode(request.getBagTypeCode())
                    .bagTypeName(request.getBagTypeName())
                    .description(request.getDescription())
                    .maxComponents(request.getMaxComponents())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Blood Bag Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodBagTypeResponse> update(
            Long id, MasBloodBagTypeRequest request) {

        log.info("Updating Blood Bag Type id={}", id);
        try {
            MasBloodBagType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Bag Type not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setBagTypeCode(request.getBagTypeCode());
            entity.setBagTypeName(request.getBagTypeName());
            entity.setDescription(request.getDescription());
            entity.setMaxComponents(request.getMaxComponents());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Blood Bag Type id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodBagTypeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Blood Bag Type status id={}, status={}", id, status);
        try {
            MasBloodBagType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Bag Type not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Blood Bag Type status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasBloodBagTypeResponse toResponse(MasBloodBagType e) {
        MasBloodBagTypeResponse res = new MasBloodBagTypeResponse();
        res.setBagTypeId(e.getBagTypeId());
        res.setBagTypeCode(e.getBagTypeCode());
        res.setBagTypeName(e.getBagTypeName());
        res.setDescription(e.getDescription());
        res.setMaxComponents(e.getMaxComponents());
        res.setStatus(e.getStatus());
        res.setLastUpdateDate(e.getLastUpdateDate());
        res.setCreatedBy(e.getCreatedBy());
        res.setLastUpdatedBy(e.getLastUpdatedBy());
        return res;
    }
}
