package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRelation;
import com.hims.entity.User;
import com.hims.entity.repository.MasRelationRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasRelationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import com.hims.service.MasRelationService;
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
public class MasRelationServiceImpl implements MasRelationService {
    private static final Logger log = LoggerFactory.getLogger(MasRelationServiceImpl.class);

    @Autowired
    private MasRelationRepository masRelationRepository;

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

    public ApiResponse<List<MasRelationResponse>> getAllRelations(int flag) {
        List<MasRelation> relations;

        if (flag == 1) {
            // Fetch only records with status 'Y'
            relations = masRelationRepository.findByStatusIgnoreCaseOrderByRelationNameAsc("Y");
        } else if (flag == 0) {
            // Fetch all records with status 'Y' or 'N'
            relations = masRelationRepository.findAllByOrderByStatusDescLastChgDateDesc();
        } else {
            // Handle invalid flag values
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    public ApiResponse<List<MasRelationResponse>> getAllRelations() {
        List<MasRelation> relations = masRelationRepository.findAll();

        List<MasRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
    }

    private MasRelationResponse convertToResponse(MasRelation relation) {
        MasRelationResponse response = new MasRelationResponse();
        response.setId(relation.getId());
        response.setRelationName(relation.getRelationName());
        response.setCode(relation.getCode());
        response.setStatus(relation.getStatus());
        response.setLastChgBy(relation.getLastChgBy());
        response.setLastChgDate(relation.getLastChgDate());
        return response;
    }


    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> addRelation(MasRelationRequest relationRequest) {
        try{
            MasRelation relation = new MasRelation();
            relation.setRelationName(relationRequest.getRelationName());
            relation.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            relation.setLastChgBy(String.valueOf(currentUser.getUserId()));
            relation.setLastChgDate(LocalDateTime.now());
            relation.setCode(relationRequest.getCode());

            MasRelation savedRelation = masRelationRepository.save(relation);
            return ResponseUtils.createSuccessResponse(convertToResponse(savedRelation), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> updateRelation(Long id, MasRelationRequest relationRequest) {
        try{
            Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
            if (existingRelationOpt.isPresent()) {
                MasRelation existingRelation = existingRelationOpt.get();
                existingRelation.setRelationName(relationRequest.getRelationName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingRelation.setLastChgBy(String.valueOf(currentUser.getUserId()));
                existingRelation.setLastChgDate(LocalDateTime.now());
                existingRelation.setCode(relationRequest.getCode());

                MasRelation updatedRelation = masRelationRepository.save(existingRelation);
                return ResponseUtils.createSuccessResponse(convertToResponse(updatedRelation), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
                }, "Relation not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> changeRelationsStatus(Long id, String status) {
        try{
            Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
            if (existingRelationOpt.isPresent()) {
                MasRelation existingRelation = existingRelationOpt.get();

                // Validate status value
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
                    }, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }

                existingRelation.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingRelation.setLastChgBy(String.valueOf(currentUser.getUserId()));
                existingRelation.setLastChgDate(LocalDateTime.now());

                MasRelation updatedRelation = masRelationRepository.save(existingRelation);

                return ResponseUtils.createSuccessResponse(convertToResponse(updatedRelation), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
                }, "Relation not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> findById(Long id) {
        Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
        if (existingRelationOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingRelationOpt.get()), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
            }, "Relation not found", 404);
        }
    }
}