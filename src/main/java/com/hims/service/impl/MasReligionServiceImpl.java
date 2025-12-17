package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasReligion;
import com.hims.entity.User;
import com.hims.entity.repository.MasReligionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasReligionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;
import com.hims.service.MasReligionService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasReligionServiceImpl implements MasReligionService {

    private static final Logger log = LoggerFactory.getLogger(MasReligionServiceImpl.class);

    @Autowired
    private MasReligionRepository masReligionRepository;

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
    public ApiResponse<List<MasReligionResponse>> getAllReligions(int flag) {
        List<MasReligion> religions;

        if (flag == 1) {
            religions = masReligionRepository.findByStatusIgnoreCaseOrderByNameAsc("Y");
        } else if (flag == 0) {
            religions = masReligionRepository.findAllByOrderByStatusDescLastChgDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasReligionResponse> responses = religions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> addReligion(MasReligionRequest religionRequest) {
        try{
            MasReligion religion = new MasReligion();
            religion.setName(religionRequest.getName());
            religion.setStatus("y");

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            religion.setLastChgBy(String.valueOf(currentUser.getUserId()));
            religion.setLastChgDate(LocalDateTime.now());

            MasReligion savedReligion = masReligionRepository.save(religion);
            return ResponseUtils.createSuccessResponse(convertToResponse(savedReligion), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> updateReligion(Long id, MasReligionRequest religionRequest) {
        try{
            Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
            if (existingReligionOpt.isPresent()) {
                MasReligion existingReligion = existingReligionOpt.get();
                existingReligion.setName(religionRequest.getName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingReligion.setLastChgBy(String.valueOf(currentUser.getUserId()));
                existingReligion.setLastChgDate(LocalDateTime.now());

                MasReligion updatedReligion = masReligionRepository.save(existingReligion);
                return ResponseUtils.createSuccessResponse(convertToResponse(updatedReligion), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {
                }, "Religion not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> changeReligionStatus(Long id, String status) {
        try{
            Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
            if (existingReligionOpt.isPresent()) {
                MasReligion existingReligion = existingReligionOpt.get();

                // Validate status value
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {
                    }, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }

                existingReligion.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingReligion.setLastChgBy(String.valueOf(currentUser.getUserId()));
                existingReligion.setLastChgDate(LocalDateTime.now());
                MasReligion updatedReligion = masReligionRepository.save(existingReligion);

                return ResponseUtils.createSuccessResponse(convertToResponse(updatedReligion), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {
                }, "Religion not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> findById(Long id) {
        Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
        if (existingReligionOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingReligionOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {}, "Religion not found", 404);
        }
    }

    private MasReligionResponse convertToResponse(MasReligion religion) {
        MasReligionResponse response = new MasReligionResponse();
        response.setId(religion.getId());
        response.setName(religion.getName());
        response.setStatus(religion.getStatus());
        response.setLastChgBy(religion.getLastChgBy());
        response.setLastChgDate(religion.getLastChgDate());
        return response;
    }
}