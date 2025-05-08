package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasGender;
import com.hims.entity.User;
import com.hims.entity.repository.MasGenderRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.helperUtil.HelperUtils;
import com.hims.request.MasGenderRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasGenderService;
import com.hims.utils.ResponseUtils;
import lombok.NoArgsConstructor;
import lombok.experimental.Helper;
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
@NoArgsConstructor
public class MasGenderServiceImpl implements MasGenderService {
    private static final Logger log = LoggerFactory.getLogger(MasGenderServiceImpl.class);

    @Autowired
    private MasGenderRepository masGenderRepository;

    @Autowired
    UserRepo userRepo;

    public ApiResponse<List<MasGenderResponse>> getAllGenders(int flag) {
        List<MasGender> genders;

        if (flag == 1) {
            genders = masGenderRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            genders = masGenderRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasGenderResponse> responses = genders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasGenderResponse convertToResponse(MasGender gender) {
        MasGenderResponse response = new MasGenderResponse();
        response.setId(gender.getId());
        response.setGenderCode(gender.getGenderCode());
        response.setGenderName(gender.getGenderName());
        response.setLastChgDt(gender.getLastChgDt());
        response.setStatus(gender.getStatus());
        response.setCode(gender.getCode());
        response.setCode(gender.getLastChgBy());

        return response;
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
    @Transactional
    public ApiResponse<MasGenderResponse> addGender(MasGenderRequest genderRequest) {
        try{
            MasGender gender = new MasGender();
            gender.setGenderCode(genderRequest.getGenderCode());
            gender.setGenderName(genderRequest.getGenderName());
            gender.setLastChgDt(LocalDateTime.now());
            gender.setStatus(genderRequest.getStatus());
            gender.setCode(null);

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            gender.setLastChgBy(String.valueOf(currentUser.getUserId()));


            MasGender savedGender = masGenderRepository.save(gender);
            return ResponseUtils.createSuccessResponse(convertToResponse(savedGender), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> updateGender(Long id, MasGenderResponse genderDetails) {
        try{
            Optional<MasGender> existingGenderOpt = masGenderRepository.findById(id);
            if (existingGenderOpt.isPresent()) {
                MasGender existingGender = existingGenderOpt.get();
                existingGender.setGenderCode(genderDetails.getGenderCode());
                existingGender.setGenderName(genderDetails.getGenderName());
                existingGender.setLastChgDt(LocalDateTime.now());
                existingGender.setStatus(genderDetails.getStatus());
                existingGender.setCode(null);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingGender.setLastChgBy(String.valueOf(currentUser.getUserId()));


                MasGender updatedGender = masGenderRepository.save(existingGender);
                return ResponseUtils.createSuccessResponse(convertToResponse(updatedGender), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {
                }, "Gender not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> changeStatus(Long id, String status) {
        try{
            Optional<MasGender> existingGenderOpt = masGenderRepository.findById(id);
            if (existingGenderOpt.isPresent()) {
                MasGender existingGender = existingGenderOpt.get();

                // Ensure the status is either "Y" (Active) or "N" (Inactive)
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {
                    }, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }
                existingGender.setLastChgDt(LocalDateTime.now());
                existingGender.setStatus(status); // Set status as "Y" or "N"
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingGender.setLastChgBy(String.valueOf(currentUser.getUserId()));

                MasGender updatedGender = masGenderRepository.save(existingGender);

                return ResponseUtils.createSuccessResponse(convertToResponse(updatedGender), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {
                }, "Gender not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> findById(Long id) {
        Optional<MasGender> existingGenderOpt = masGenderRepository.findById(id);
        if (existingGenderOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingGenderOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {}, "Gender not found", 404);
        }
    }


}
