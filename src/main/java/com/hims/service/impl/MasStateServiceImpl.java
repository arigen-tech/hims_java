package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasState;
import com.hims.entity.repository.MasStateRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.service.MasStateService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasStateServiceImpl implements MasStateService {

    @Autowired
    private MasStateRepository masStateRepository;

    @Override
    public ApiResponse<List<MasStateResponse>> getAllStates() {
        List<MasState> states = masStateRepository.findAll();

        List<MasStateResponse> responses = states.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasStateResponse convertToResponse(MasState state) {
        MasStateResponse response = new MasStateResponse();
        response.setId(state.getId());
        response.setStateCode(state.getStateCode());
        response.setStateName(state.getStateName());
        response.setStatus(state.getStatus());
        response.setLastChgBy(state.getLastChgBy());
        response.setLastChgDate(state.getLastChgDate());
        response.setLastChgTime(state.getLastChgTime());

        if (state.getCountry() != null) {
            response.setCountryId(state.getCountry().getId());
            response.setCountryName(state.getCountry().getCountryName());
        }

        return response;
    }
}
