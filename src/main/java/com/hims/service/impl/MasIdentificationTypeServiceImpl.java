package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasIdentificationType;
import com.hims.entity.User;
import com.hims.entity.repository.MasIdentificationTypeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasIdentificationTypeRequest;
import com.hims.response.MasIdentificationTypeResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasIdentificationTypeService;
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
public class MasIdentificationTypeServiceImpl implements MasIdentificationTypeService {

    private static final Logger log = LoggerFactory.getLogger(MasIdentificationTypeServiceImpl.class);

    @Autowired
    private MasIdentificationTypeRepository masIdentificationTypeRepository;

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
    public ApiResponse<MasIdentificationTypeResponse> addIdentificationType(MasIdentificationTypeRequest request) {
        try{
            MasIdentificationType type = new MasIdentificationType();
            type.setIdentificationCode(request.getIdentificationCode());
            type.setIdentificationName(request.getIdentificationName());
            type.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            type.setLastChangedBy(currentUser.getUserId());
            type.setLastChangedDate(Instant.now());
            type.setMapId(request.getMapId());

            MasIdentificationType savedType = masIdentificationTypeRepository.save(type);
            return ResponseUtils.createSuccessResponse(mapToResponse(savedType), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeIdentificationStatus(Long id, String statusValue) {
        try{
            Optional<MasIdentificationType> typeOpt = masIdentificationTypeRepository.findById(id);
            if (typeOpt.isPresent()) {
                MasIdentificationType type = typeOpt.get();
                type.setStatus(statusValue);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                type.setLastChangedBy(currentUser.getUserId());
                type.setLastChangedDate(Instant.now());
                masIdentificationTypeRepository.save(type);
                return ResponseUtils.createSuccessResponse("Identification type status updated", new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Identification type not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasIdentificationTypeResponse> editIdentificationType(Long id, MasIdentificationTypeRequest request) {
        try{
            Optional<MasIdentificationType> typeOpt = masIdentificationTypeRepository.findById(id);
            if (typeOpt.isPresent()) {
                MasIdentificationType type = typeOpt.get();
                type.setIdentificationCode(request.getIdentificationCode());
                type.setIdentificationName(request.getIdentificationName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                type.setLastChangedBy(currentUser.getUserId());
                type.setLastChangedDate(Instant.now());
                type.setMapId(request.getMapId());

                masIdentificationTypeRepository.save(type);
                return ResponseUtils.createSuccessResponse(mapToResponse(type), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Identification type not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasIdentificationTypeResponse> getIdentificationTypeById(Long id) {
        return masIdentificationTypeRepository.findById(id)
                .map(type -> ResponseUtils.createSuccessResponse(mapToResponse(type), new TypeReference<>() {
                }))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Identification type not found", 404));
    }

    @Override
    public ApiResponse<List<MasIdentificationTypeResponse>> getAllIdentificationTypes(int flag) {
        List<MasIdentificationType> types;

        if (flag == 1) {
            types = masIdentificationTypeRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            types = masIdentificationTypeRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasIdentificationTypeResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasIdentificationTypeResponse mapToResponse(MasIdentificationType type) {
        MasIdentificationTypeResponse response = new MasIdentificationTypeResponse();
        response.setIdentificationTypeId(type.getIdentificationTypeId());
        response.setIdentificationCode(type.getIdentificationCode());
        response.setIdentificationName(type.getIdentificationName());
        response.setStatus(type.getStatus());
        response.setLastChangedBy(type.getLastChangedBy());
        response.setLastChangedDate(type.getLastChangedDate());
        response.setMapId(type.getMapId());
        return response;
    }
}