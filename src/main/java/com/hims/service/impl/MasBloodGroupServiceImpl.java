package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodGroup;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodGroupRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasBloodGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodGroupResponse;
import com.hims.service.MasBloodGroupService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasBloodGroupServiceImpl implements MasBloodGroupService {

    private static final Logger log = LoggerFactory.getLogger(MasBloodGroupServiceImpl.class);

    @Autowired
    private MasBloodGroupRepository masBloodGroupRepository;

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
    public ApiResponse<List<MasBloodGroupResponse>> getAllBloodGroups(int flag) {
        List<MasBloodGroup> bloodGroups;

        if (flag == 1) {
            // Fetch only records with status 'Y'
            bloodGroups = masBloodGroupRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            // Fetch all records with status 'Y' or 'N'
            bloodGroups = masBloodGroupRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            // Handle invalid flag values
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasBloodGroupResponse> responses = bloodGroups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> addBloodGroup(MasBloodGroupRequest bloodGroupRequest) {
        try{
            MasBloodGroup bloodGroup = new MasBloodGroup();
            bloodGroup.setBloodGroupCode(bloodGroupRequest.getBloodGroupCode());
            bloodGroup.setBloodGroupName(bloodGroupRequest.getBloodGroupName());
            bloodGroup.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            bloodGroup.setLastChangedBy(String.valueOf(currentUser.getUserId()));
            bloodGroup.setLastChangedDate(Instant.now());
            bloodGroup.setLastChangedTime(getCurrentTimeFormatted());
            //bloodGroup.setHicCode(bloodGroupRequest.getHicCode());

            MasBloodGroup savedBloodGroup = masBloodGroupRepository.save(bloodGroup);
            return ResponseUtils.createSuccessResponse(convertToResponse(savedBloodGroup), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> updateBloodGroup(Long id, MasBloodGroupRequest bloodGroupRequest) {
        try{
            Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
            if (existingBloodGroupOpt.isPresent()) {
                MasBloodGroup existingBloodGroup = existingBloodGroupOpt.get();
                existingBloodGroup.setBloodGroupCode(bloodGroupRequest.getBloodGroupCode());
                existingBloodGroup.setBloodGroupName(bloodGroupRequest.getBloodGroupName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingBloodGroup.setLastChangedBy(String.valueOf(currentUser.getUserId()));
                existingBloodGroup.setLastChangedDate(Instant.now());
                existingBloodGroup.setLastChangedTime(getCurrentTimeFormatted());
                //existingBloodGroup.setHicCode(bloodGroupDetails.getHicCode());

                MasBloodGroup updatedBloodGroup = masBloodGroupRepository.save(existingBloodGroup);
                return ResponseUtils.createSuccessResponse(convertToResponse(updatedBloodGroup), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {
                }, "Blood Group not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> changeBloodGroupStatus(Long id, String status) {
        try{
            Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
            if (existingBloodGroupOpt.isPresent()) {
                MasBloodGroup existingBloodGroup = existingBloodGroupOpt.get();

                // Validate status value
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {
                    }, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }

                existingBloodGroup.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingBloodGroup.setLastChangedBy(String.valueOf(currentUser.getUserId()));
                existingBloodGroup.setLastChangedDate(Instant.now());
                existingBloodGroup.setLastChangedTime(getCurrentTimeFormatted());
                MasBloodGroup updatedBloodGroup = masBloodGroupRepository.save(existingBloodGroup);

                return ResponseUtils.createSuccessResponse(convertToResponse(updatedBloodGroup), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {
                }, "Blood Group not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> findById(Long id) {
        Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
        if (existingBloodGroupOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingBloodGroupOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {}, "Blood Group not found", 404);
        }
    }

    private MasBloodGroupResponse convertToResponse(MasBloodGroup bloodGroup) {
        MasBloodGroupResponse response = new MasBloodGroupResponse();
        response.setBloodGroupId(bloodGroup.getBloodGroupId());
        response.setBloodGroupCode(bloodGroup.getBloodGroupCode());
        response.setBloodGroupName(bloodGroup.getBloodGroupName());
        response.setStatus(bloodGroup.getStatus());
        response.setLastChangedBy(bloodGroup.getLastChangedBy());
        response.setLastChangedDate(bloodGroup.getLastChangedDate());
        response.setLastChangedTime(bloodGroup.getLastChangedTime());
        response.setHicCode(bloodGroup.getHicCode());
        return response;
    }
}