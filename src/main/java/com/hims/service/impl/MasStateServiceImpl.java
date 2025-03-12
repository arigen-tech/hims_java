package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCountry;
import com.hims.entity.MasState;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.entity.repository.MasStateRepository;
import com.hims.request.MasStateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.service.MasStateService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasStateServiceImpl implements MasStateService {

    @Autowired
    private MasStateRepository masStateRepository;

    @Autowired
    private MasCountryRepository masCountryRepository;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasStateResponse> addState(MasStateRequest request) {
        Optional<MasCountry> countryOpt = masCountryRepository.findById(request.getCountryId());
        if (countryOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Country not found", 404);
        }
        MasState state = new MasState();
        state.setStateCode(request.getStateCode());
        state.setStateName(request.getStateName());
        state.setStatus(request.getStatus());
        state.setLastChgBy(request.getLastChgBy());
        state.setLastChgDate(Instant.now());
        state.setLastChgTime(getCurrentTimeFormatted());
        state.setCountry(countryOpt.get());

        MasState savedState = masStateRepository.save(state);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedState), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeStateStatus(Long id, String status) {
        Optional<MasState> stateOpt = masStateRepository.findById(id);
        if (stateOpt.isPresent()) {
            MasState state = stateOpt.get();
            state.setStatus(status);
            state.setLastChgDate(Instant.now());
            masStateRepository.save(state);
            return ResponseUtils.createSuccessResponse("State status updated", new TypeReference<>() {});
        }
        return ResponseUtils.createNotFoundResponse("State not found", 404);
    }

    @Override
    public ApiResponse<MasStateResponse> editState(Long id, MasStateRequest request) {
        Optional<MasState> stateOpt = masStateRepository.findById(id);
        if (stateOpt.isPresent()) {
            MasState state = stateOpt.get();
            state.setStateCode(request.getStateCode());
            state.setStateName(request.getStateName());
            state.setStatus(request.getStatus());
            state.setLastChgBy(request.getLastChgBy());
            state.setLastChgDate(Instant.now());
            state.setLastChgTime(getCurrentTimeFormatted());

            if (request.getCountryId() != null) {
                Optional<MasCountry> countryOpt = masCountryRepository.findById(request.getCountryId());
                if (countryOpt.isPresent()) {
                    state.setCountry(countryOpt.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("Country not found", 404);
                }
            }

            masStateRepository.save(state);
            return ResponseUtils.createSuccessResponse(mapToResponse(state), new TypeReference<>() {});
        }
        return ResponseUtils.createNotFoundResponse("State not found", 404);
    }

    @Override
    public ApiResponse<MasStateResponse> getStateById(Long id) {
        return masStateRepository.findById(id)
                .map(state -> ResponseUtils.createSuccessResponse(mapToResponse(state), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("State not found", 404));
    }

    @Override
    public ApiResponse<List<MasStateResponse>> getAllStates() {
        List<MasStateResponse> states = masStateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(states, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasStateResponse>> getStatesByCountryId(Long countryId) {
        List<MasStateResponse> states = masStateRepository.findByCountryId(countryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(states, new TypeReference<>() {});
    }

    private MasStateResponse mapToResponse(MasState state) {
        MasStateResponse response = new MasStateResponse();
        response.setId(state.getId());
        response.setStateCode(state.getStateCode());
        response.setStateName(state.getStateName());
        response.setStatus(state.getStatus());
        response.setLastChgBy(state.getLastChgBy());
        response.setLastChgDate(state.getLastChgDate());
        response.setLastChgTime(state.getLastChgTime());
        response.setCountryId(state.getCountry().getId());
        return response;
    }
}
