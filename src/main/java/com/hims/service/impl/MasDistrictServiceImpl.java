package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDistrict;
import com.hims.entity.MasState;
import com.hims.entity.User;
import com.hims.entity.repository.MasDistrictRepository;
import com.hims.entity.repository.MasStateRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasDistrictRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.service.MasDistrictService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasDistrictServiceImpl implements MasDistrictService {

    private static final Logger log = LoggerFactory.getLogger(MasDistrictServiceImpl.class);

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    @Autowired
    private MasStateRepository masStateRepository;

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
    public ApiResponse<MasDistrictResponse> addDistrict(MasDistrictRequest request) {
        try{
            MasState state = masStateRepository.findById(request.getStateId()).orElse(null);
            if (state == null) {
                return ResponseUtils.createNotFoundResponse("State not found", 404);
            }

            MasDistrict district = new MasDistrict();
            district.setDistrictName(request.getDistrictName());
            district.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            district.setLasChBy(String.valueOf(currentUser.getUserId()));
            district.setLastChgDate(Instant.now());
            district.setState(state);

            MasDistrict savedDistrict = masDistrictRepository.save(district);
            return ResponseUtils.createSuccessResponse(mapToResponse(savedDistrict), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeDistrictStatus(Long id, String status) {
        try{
            Optional<MasDistrict> districtOpt = masDistrictRepository.findById(id);
            if (districtOpt.isPresent()) {
                MasDistrict district = districtOpt.get();
                district.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                district.setLasChBy(String.valueOf(currentUser.getUserId()));
                district.setLastChgDate(Instant.now());
                masDistrictRepository.save(district);
                return ResponseUtils.createSuccessResponse("District status updated", new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("District not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasDistrictResponse> editDistrict(Long id, MasDistrictRequest request) {
        try{
            Optional<MasDistrict> districtOpt = masDistrictRepository.findById(id);
            if (districtOpt.isPresent()) {
                MasDistrict district = districtOpt.get();
                MasState newState = masStateRepository.findById(request.getStateId()).orElse(null);
                if (newState == null) {
                    return ResponseUtils.createNotFoundResponse("State not found", 404);
                }
                district.setDistrictName(request.getDistrictName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                district.setLasChBy(String.valueOf(currentUser.getUserId()));
                district.setLastChgDate(Instant.now());
                district.setState(newState);
                masDistrictRepository.save(district);
                return ResponseUtils.createSuccessResponse(mapToResponse(district), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("District not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasDistrictResponse> getDistrictById(Long id) {
        return masDistrictRepository.findById(id)
                .map(district -> ResponseUtils.createSuccessResponse(mapToResponse(district), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("District not found", 404));
    }

    @Override
    public ApiResponse<List<MasDistrictResponse>> getAllDistricts(int flag) {
        List<MasDistrict> districts;

        if (flag == 1) {
            districts = masDistrictRepository.findByStatusIgnoreCaseOrderByDistrictNameAsc("Y");
        } else if (flag == 0) {
            districts = masDistrictRepository.findAllByOrderByStatusDescLastChgDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasDistrictResponse> responses = districts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasDistrictResponse>> getDistrictsByStateId(Long stateId) {
//        List<MasDistrictResponse> districts = masDistrictRepository.findByStateIdAndStatusIgnoreCaseOrderByDistrictName(stateId, "y").stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//        return ResponseUtils.createSuccessResponse(districts, new TypeReference<>() {});
        List<MasDistrictResponse> districts =
                masDistrictRepository
                        .findByStateIdAndStatusIgnoreCaseOrderByDistrictNameAsc(stateId, "y")
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(districts, new TypeReference<>() {});
    }

    private MasDistrictResponse mapToResponse(MasDistrict district) {
        MasDistrictResponse response = new MasDistrictResponse();
        response.setId(district.getId());
        response.setDistrictName(district.getDistrictName());
        response.setStatus(district.getStatus());
        response.setLasChBy(district.getLasChBy());
        response.setLastChgDate(district.getLastChgDate());
        response.setStateId(district.getState().getId());
        return response;
    }
}
