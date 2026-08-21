package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIpdTransferReason;
import com.hims.entity.repository.MasIpdTransferReasonRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasIpdTransferReasonResponse;
import com.hims.service.MasIpdTransferReasonService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasIpdTransferReasonServiceImpl implements MasIpdTransferReasonService {
    @Autowired
    MasIpdTransferReasonRepository masIpdTransferReasonRepository;
    @Override
    public ApiResponse<List<MasIpdTransferReasonResponse>> getAll(int flag) {

        log.info("Fetching IPD Transfer Reason list, flag={}", flag);

        try {

            List<MasIpdTransferReason> list = (flag == 1)
                    ? masIpdTransferReasonRepository
                    .findByStatusIgnoreCaseOrderByTransferReasonNameAsc(
                            AppConstants.STATUS_Y.toLowerCase()
                    )
                    : masIpdTransferReasonRepository
                    .findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasIpdTransferReasonResponse> responseList = list.stream()
                    .map(this::mapToTransferReasonResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching IPD Transfer Reason list, flag={}", flag, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }
    private MasIpdTransferReasonResponse mapToTransferReasonResponse(MasIpdTransferReason entity) {
        return MasIpdTransferReasonResponse.builder()
                .transferReasonId(entity.getTransferReasonId())
                .transferReasonName(entity.getTransferReasonName())
                .description(entity.getDescription())
                .status(entity.getStatus())

                .build();
    }
}
