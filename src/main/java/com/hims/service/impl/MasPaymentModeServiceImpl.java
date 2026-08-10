package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasPaymentMode;
import com.hims.entity.repository.MasPaymentModeRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasPaymentModeResponse;
import com.hims.service.MasPaymentModeService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasPaymentModeServiceImpl implements MasPaymentModeService {

    @Autowired
    private MasPaymentModeRepository repository;

    @Override
    public ApiResponse<List<MasPaymentModeResponse>> getAll(int flag) {

        log.info("Fetching payment mode list, flag={}", flag);

        try {

            List<MasPaymentMode> paymentModes = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByModeNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            List<MasPaymentModeResponse> response = paymentModes.stream()
                    .map(this::mapToResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {

            log.error("Error fetching payment mode list, flag={}", flag, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private MasPaymentModeResponse mapToResponse(MasPaymentMode entity) {

        return MasPaymentModeResponse.builder()
                .paymentModeId(entity.getPaymentModeId())
                .modeCode(entity.getModeCode())
                .modeName(entity.getModeName())
                .build();
    }
}