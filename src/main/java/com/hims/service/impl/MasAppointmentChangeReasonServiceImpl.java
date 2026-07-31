package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAppointmentChangeReason;
import com.hims.entity.repository.MasAppointmentChangeReasonRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasAppointmentChangeReasonResponse;
import com.hims.response.MasOpdSessionResponse;
import com.hims.service.MasAppointmentChangeReasonService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MasAppointmentChangeReasonServiceImpl implements MasAppointmentChangeReasonService {

    private final MasAppointmentChangeReasonRepository reasonRepository;

    @Override
    public ApiResponse<List<MasAppointmentChangeReasonResponse>> getAllReasons(int flag) {
        log.info("Fetching Appointment Cancel/Change Reasons, flag={}", flag);
        try {
            List<MasAppointmentChangeReason> list =
                    (flag == 1)
                            ? reasonRepository.findByStatusIgnoreCaseOrderByReasonNameAsc("y")
                            : reasonRepository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream()
                            .map(this::convertToResponse)
                            .toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Appointment Cancel/Change Reasons", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong",
                    500
            );
        }
    }


    private MasAppointmentChangeReasonResponse convertToResponse(MasAppointmentChangeReason masAppointmentChangeReason){
    MasAppointmentChangeReasonResponse masAppointmentReasonResponse = new MasAppointmentChangeReasonResponse();
    masAppointmentReasonResponse.setReasonId(masAppointmentChangeReason.getReasonId());
    masAppointmentReasonResponse.setReasonName(masAppointmentChangeReason.getReasonName());
    masAppointmentReasonResponse.setReasonCode(masAppointmentChangeReason.getReasonCode());
    masAppointmentReasonResponse.setStatus(masAppointmentChangeReason.getStatus());
    masAppointmentReasonResponse.setCreatedBy(masAppointmentChangeReason.getCreatedBy());
    masAppointmentReasonResponse.setLastUpdatedBy(masAppointmentChangeReason.getLastUpdatedBy());
    masAppointmentReasonResponse.setLastUpdateDate(masAppointmentChangeReason.getLastUpdateDate());

    return masAppointmentReasonResponse;
    }
}
