package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCountry;
import com.hims.entity.MasState;
import com.hims.entity.User;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.entity.repository.MasStateRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasStateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.service.MasStateService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasStateServiceImpl implements MasStateService {

    private static final Logger log = LoggerFactory.getLogger(MasStateServiceImpl.class);

    @Autowired
    private MasStateRepository masStateRepository;

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Autowired
    private UserRepo userRepo;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    @Override
    public ApiResponse<MasStateResponse> addState(MasStateRequest request) {
        try{
            Optional<MasCountry> countryOpt = masCountryRepository.findById(request.getCountryId());
            if (countryOpt.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Country not found", 404);
            }
            MasState state = new MasState();
            state.setStateCode(request.getStateCode());
            state.setStateName(request.getStateName());
            state.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            state.setLastChgBy(String.valueOf(currentUser.getUserId()));
            state.setLastChgDate(Instant.now());
            state.setLastChgTime(getCurrentTimeFormatted());
            state.setCountry(countryOpt.get());

            MasState savedState = masStateRepository.save(state);
            return ResponseUtils.createSuccessResponse(mapToResponse(savedState), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeStateStatus(Long id, String status) {
        try{
            Optional<MasState> stateOpt = masStateRepository.findById(id);
            if (stateOpt.isPresent()) {
                MasState state = stateOpt.get();
                state.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                state.setLastChgBy(String.valueOf(currentUser.getUserId()));
                state.setLastChgDate(Instant.now());
                masStateRepository.save(state);
                return ResponseUtils.createSuccessResponse("State status updated", new TypeReference<>() {
                });
            }
            return ResponseUtils.createNotFoundResponse("State not found", 404);
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasStateResponse> editState(Long id, MasStateRequest request) {
        try{
            Optional<MasState> stateOpt = masStateRepository.findById(id);
            if (stateOpt.isPresent()) {
                MasState state = stateOpt.get();
                state.setStateCode(request.getStateCode());
                state.setStateName(request.getStateName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                state.setLastChgBy(String.valueOf(currentUser.getUserId()));
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
                return ResponseUtils.createSuccessResponse(mapToResponse(state), new TypeReference<>() {
                });
            }
            return ResponseUtils.createNotFoundResponse("State not found", 404);
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasStateResponse> getStateById(Long id) {
        return masStateRepository.findById(id)
                .map(state -> ResponseUtils.createSuccessResponse(mapToResponse(state), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("State not found", 404));
    }

    @Override
    public ApiResponse<List<MasStateResponse>> getAllStates(int flag) {
        List<MasState> states;

        if (flag == 1) {
            states = masStateRepository.findByStatusIgnoreCaseOrderByStateNameAsc("y");
        } else if (flag == 0) {
            states = masStateRepository.findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasStateResponse> responses = states.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<List<MasStateResponse>> getStatesByCountryId(Long countryId) {
        List<MasStateResponse> states = masStateRepository.findByCountryIdAndStatusIgnoreCaseOrderByStateNameAsc(countryId, "Y").stream()
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
