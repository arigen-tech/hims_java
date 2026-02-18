package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodDonationType;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodDonationTypeRepository;
import com.hims.request.MasBloodDonationTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDonationTypeResponse;
import com.hims.service.MasBloodDonationTypeService;
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
public class MasBloodDonationTypeServiceImpl
        implements MasBloodDonationTypeService {

    private final MasBloodDonationTypeRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasBloodDonationTypeResponse>> getAll(int flag) {
        log.info("Fetching Blood Donation Type list, flag={}", flag);
        try {
            List<MasBloodDonationType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByDonationTypeNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Blood Donation Type list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationTypeResponse> getById(Long id) {
        log.info("Fetching Blood Donation Type by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood Donation Type not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Blood Donation Type by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationTypeResponse> create(
            MasBloodDonationTypeRequest request) {

        log.info("Creating Blood Donation Type");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasBloodDonationType entity = MasBloodDonationType.builder()
                    .donationTypeCode(request.getDonationTypeCode())
                    .donationTypeName(request.getDonationTypeName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Blood Donation Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationTypeResponse> update(
            Long id, MasBloodDonationTypeRequest request) {

        log.info("Updating Blood Donation Type id={}", id);
        try {
            MasBloodDonationType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Donation Type not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setDonationTypeCode(request.getDonationTypeCode());
            entity.setDonationTypeName(request.getDonationTypeName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Blood Donation Type id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationTypeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Blood Donation Type status id={}, status={}", id, status);
        try {
            MasBloodDonationType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Donation Type not found", 404);
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

            entity.setStatus(status);
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Blood Donation Type status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasBloodDonationTypeResponse toResponse(MasBloodDonationType e) {
        MasBloodDonationTypeResponse res = new MasBloodDonationTypeResponse();
        res.setDonationTypeId(e.getDonationTypeId());
        res.setDonationTypeCode(e.getDonationTypeCode());
        res.setDonationTypeName(e.getDonationTypeName());
        res.setDescription(e.getDescription());
        res.setStatus(e.getStatus());
        res.setLastUpdateDate(e.getLastUpdateDate());
        res.setCreatedBy(e.getCreatedBy());
        res.setLastUpdateBy(e.getLastUpdatedBy());
        return res;
    }
}
