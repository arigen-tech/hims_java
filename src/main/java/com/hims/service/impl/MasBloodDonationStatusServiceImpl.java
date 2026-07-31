package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodDonationStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodDonationStatusRepository;
import com.hims.request.MasBloodDonationStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDonationStatusResponse;
import com.hims.service.MasBloodDonationStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class MasBloodDonationStatusServiceImpl implements MasBloodDonationStatusService {

    private final MasBloodDonationStatusRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasBloodDonationStatusResponse>> getAll(int flag) {
        log.info("Fetching Blood Donation Status list, flag={}", flag);
        try {
            List<MasBloodDonationStatus> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByDonationStatusNameAsc("y")
                            : repository.findAllByOrderByStatusDescCreatedDate();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Blood Donation Status list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationStatusResponse> getById(Long id) {
        log.info("Fetching Blood Donation Status by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood Donation Status not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Blood Donation Status by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationStatusResponse> create(
            MasBloodDonationStatusRequest request) {

        log.info("Creating Blood Donation Status");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasBloodDonationStatus entity = MasBloodDonationStatus.builder()
                    .donationStatusCode(request.getDonationStatusCode())
                    .donationStatusName(request.getDonationStatusName())
                    .description(request.getDescription())
                    .isFinal(request.getIsFinal())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .createdDate(LocalDateTime.now())
                    .build();
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Blood Donation Status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationStatusResponse> update(
            Long id, MasBloodDonationStatusRequest request) {

        log.info("Updating Blood Donation Status id={}", id);
        try {
            MasBloodDonationStatus entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Donation Status not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setDonationStatusCode(request.getDonationStatusCode());
            entity.setDonationStatusName(request.getDonationStatusName());
            entity.setDescription(request.getDescription());
            entity.setIsFinal(request.getIsFinal());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Blood Donation Status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodDonationStatusResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Blood Donation Status id={}, status={}", id, status);
        try {
            MasBloodDonationStatus entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Donation Status not found", 404);
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


            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Blood Donation Status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasBloodDonationStatusResponse toResponse(
            MasBloodDonationStatus e) {

        MasBloodDonationStatusResponse res = new MasBloodDonationStatusResponse();
        res.setDonationStatusId(e.getDonationStatusId());
        res.setDonationStatusCode(e.getDonationStatusCode());
        res.setDonationStatusName(e.getDonationStatusName());
        res.setDescription(e.getDescription());
        res.setIsFinal(e.getIsFinal());
        res.setStatus(e.getStatus());
        res.setCreatedBy(e.getCreatedBy());
        res.setCreatedDate(e.getCreatedDate());

        return res;
    }
}
