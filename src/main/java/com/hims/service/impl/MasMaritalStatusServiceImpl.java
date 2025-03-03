package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMaritalStatus;
import com.hims.entity.repository.MasMaritalStatusRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;
import com.hims.service.MasMaritalStatusService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasMaritalStatusServiceImpl implements MasMaritalStatusService {

    @Autowired
    private MasMaritalStatusRepository masMaritalStatusRepository;

    public ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses() {
        List<MasMaritalStatus> statuses = masMaritalStatusRepository.findAll();

        List<MasMaritalStatusResponse> responses = statuses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasMaritalStatusResponse convertToResponse(MasMaritalStatus status) {
        MasMaritalStatusResponse response = new MasMaritalStatusResponse();
        response.setId(status.getId());
        response.setName(status.getName());
        response.setStatus(status.getStatus());
        response.setLastChgBy(status.getLastChgBy());
        response.setLastChgDate(status.getLastChgDate());
        return response;
    }
}
