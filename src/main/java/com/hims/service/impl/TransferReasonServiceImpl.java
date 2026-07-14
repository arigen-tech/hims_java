package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.TransferReason;
import com.hims.entity.User;
import com.hims.entity.repository.TransferReasonRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.TransferReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTransferReasonResponse;
import com.hims.service.TransferReasonService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferReasonServiceImpl implements TransferReasonService {

    private static final Logger log = LoggerFactory.getLogger(TransferReasonServiceImpl.class);

    @Autowired
    private TransferReasonRepository transferReasonRepository;

    @Autowired
    private UserRepo userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    @Override
    public ApiResponse<List<MasTransferReasonResponse>> getAll(int flag) {

        List<TransferReason> transferReasons;

        if (flag == 1) {
            transferReasons = transferReasonRepository.findByStatusIgnoreCaseOrderByTransferReasonNameAsc("Y");
        } else if (flag == 0) {
            transferReasons = transferReasonRepository.findAllByOrderByStatusDescLastUpdateDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Invalid flag value. Use 0 or 1.",
                    400
            );
        }

        List<MasTransferReasonResponse> responses = transferReasons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasTransferReasonResponse> createTransferReason(TransferReasonRequest request) {

        try {

            TransferReason transferReason = new TransferReason();
            transferReason.setTransferReasonName(request.getReasonName());
            transferReason.setDescription(request.getDescription());
            transferReason.setStatus("Y");

            User currentUser = getCurrentUser();

            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            transferReason.setCreatedBy(String.valueOf(currentUser.getUserId()));
            transferReason.setLastUpdateDate(LocalDateTime.now());

            TransferReason saved = transferReasonRepository.save(transferReason);

            return ResponseUtils.createSuccessResponse(
                    convertToResponse(saved),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasTransferReasonResponse> updateTransferReason(Long transferReasonId, TransferReasonRequest request) {

        try {

            Optional<TransferReason> existing = transferReasonRepository.findById(transferReasonId);

            if (existing.isPresent()) {

                TransferReason transferReason = existing.get();

                transferReason.setTransferReasonName(request.getReasonName());
                transferReason.setDescription(request.getDescription());

                User currentUser = getCurrentUser();

                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {},
                            "Current user not found",
                            HttpStatus.UNAUTHORIZED.value()
                    );
                }

                transferReason.setLastUpdatedBy(String.valueOf(currentUser.getUserId()));
                transferReason.setLastUpdateDate(LocalDateTime.now());

                TransferReason updated = transferReasonRepository.save(transferReason);

                return ResponseUtils.createSuccessResponse(
                        convertToResponse(updated),
                        new TypeReference<>() {}
                );

            }

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<MasTransferReasonResponse>() {},
                    "Transfer Reason not found",
                    404
            );

        } catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasTransferReasonResponse> changeActiveStatus(Long transferReasonId, String status) {

        try {

            Optional<TransferReason> existing = transferReasonRepository.findById(transferReasonId);

            if (existing.isPresent()) {

                TransferReason transferReason = existing.get();

                if (!status.equalsIgnoreCase("Y") && !status.equalsIgnoreCase("N")) {

                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<MasTransferReasonResponse>() {},
                            "Invalid status value. Use 'Y' or 'N'.",
                            400
                    );
                }

                transferReason.setStatus(status);

                User currentUser = getCurrentUser();

                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {},
                            "Current user not found",
                            HttpStatus.UNAUTHORIZED.value()
                    );
                }

                transferReason.setLastUpdatedBy(String.valueOf(currentUser.getUserId()));
                transferReason.setLastUpdateDate(LocalDateTime.now());

                TransferReason updated = transferReasonRepository.save(transferReason);

                return ResponseUtils.createSuccessResponse(
                        convertToResponse(updated),
                        new TypeReference<>() {}
                );
            }

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<MasTransferReasonResponse>() {},
                    "Transfer Reason not found",
                    404
            );

        } catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<MasTransferReasonResponse> getById(Long transferReasonId) {

        Optional<TransferReason> transferReason = transferReasonRepository.findById(transferReasonId);

        if (transferReason.isPresent()) {

            return ResponseUtils.createSuccessResponse(
                    convertToResponse(transferReason.get()),
                    new TypeReference<>() {}
            );
        }

        return ResponseUtils.createFailureResponse(
                null,
                new TypeReference<MasTransferReasonResponse>() {},
                "Transfer Reason not found",
                404
        );
    }

    private MasTransferReasonResponse convertToResponse(TransferReason transferReason) {

        MasTransferReasonResponse response = new MasTransferReasonResponse();

        response.setId(transferReason.getTransferReasonId());
        response.setTransferReasonName(transferReason.getTransferReasonName());
        response.setCode(transferReason.getDescription());
        response.setStatus(transferReason.getStatus());
        response.setLastChgBy(transferReason.getLastUpdatedBy());
        response.setLastChgDate(transferReason.getLastUpdateDate());

        return response;
    }
}