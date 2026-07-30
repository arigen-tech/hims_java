package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDischargeReason;
import com.hims.entity.repository.MasDischargeReasonRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasDischargeReasonResponse;
import com.hims.service.MasDischargeReasonService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasDischargeReasonServiceImpl implements MasDischargeReasonService {

    @Autowired
    private MasDischargeReasonRepository repository;

    @Override
    public ApiResponse<List<MasDischargeReasonResponse>> getAll(int flag) {
        log.info("Fetching discharge reason list, flag={}", flag);

        try {
            List<MasDischargeReason> dischargeReasons = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByReasonNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastUpdateDateDesc();    

            List<MasDischargeReasonResponse> responses = dischargeReasons.stream()
                    .map(this::mapToResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error fetching discharge reason list, flag={}", flag, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    private MasDischargeReasonResponse mapToResponse(MasDischargeReason entity) {
        return MasDischargeReasonResponse.builder()
                .id(entity.getDischargeReasonId())
                .reasonCode(entity.getReasonCode())
                .reasonName(entity.getReasonName())
                .build();
    }
}
