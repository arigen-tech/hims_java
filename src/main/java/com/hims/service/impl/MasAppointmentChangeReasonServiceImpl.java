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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MasAppointmentChangeReasonServiceImpl implements MasAppointmentChangeReasonService {

    private final MasAppointmentChangeReasonRepository reasonRepository;

    @Override
    public ApiResponse<List<MasAppointmentChangeReasonResponse>> getAllReasons(int flag) {
        List<MasAppointmentChangeReason> masAppointmentChangeReasons = reasonRepository.findAll();

        List<MasAppointmentChangeReasonResponse> responses = masAppointmentChangeReasons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
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
