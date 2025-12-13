package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasSymptoms;
import com.hims.entity.User;
import com.hims.entity.repository.MasSymptomsRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasSymptomsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasCollectionResponse;
import com.hims.response.MasSymptomsResponse;
import com.hims.service.MasSymptomsService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasSymptomsServiceImpl implements MasSymptomsService {

    private static final Logger log = LoggerFactory.getLogger(MasSymptomsServiceImpl.class);

    @Autowired
    private MasSymptomsRepository symptomsRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    @Transactional
    public ApiResponse<MasSymptomsResponse> createSymptom(MasSymptomsRequest symptomsReq) {
        try{
            MasSymptoms masSymptoms = new MasSymptoms();
            masSymptoms.setSymptomsCode(symptomsReq.getSymptomsCode());
            masSymptoms.setSymptomsName(symptomsReq.getSymptomsName());
            masSymptoms.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            masSymptoms.setLastChgBy(currentUser.getUsername());
            masSymptoms.setLastChgDate(Instant.now());
            masSymptoms.setMostCommonUse(symptomsReq.getMostCommonUse());
            return ResponseUtils.createSuccessResponse(mapToResponse(symptomsRepo.save(masSymptoms)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasSymptomsResponse> updateSymptom(Long id, MasSymptomsRequest symptomsReq){
        try{
            Optional<MasSymptoms> masSymptomsOpt = symptomsRepo.findById(id);
            if (masSymptomsOpt.isPresent()) {
                MasSymptoms masSymptoms = masSymptomsOpt.get();
                masSymptoms.setSymptomsCode(symptomsReq.getSymptomsCode());
                masSymptoms.setSymptomsName(symptomsReq.getSymptomsName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                masSymptoms.setLastChgBy(currentUser.getUsername());
                masSymptoms.setLastChgDate(Instant.now());
                masSymptoms.setMostCommonUse(symptomsReq.getMostCommonUse());
                return ResponseUtils.createSuccessResponse(mapToResponse(symptomsRepo.save(masSymptoms)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasSymptomsResponse>() {
                        },
                        "Symptom not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSymptomsResponse> changeSymptomStatus(Long id, String status) {
        try{
            Optional<MasSymptoms> masSymptomsOpt = symptomsRepo.findById(id);
            if (masSymptomsOpt.isPresent()) {
                MasSymptoms masSymptoms = masSymptomsOpt.get();
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasSymptomsResponse>() {
                            },
                            "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }
                masSymptoms.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                masSymptoms.setLastChgBy(currentUser.getUsername());
                masSymptoms.setLastChgDate(Instant.now());
                return ResponseUtils.createSuccessResponse(mapToResponse(symptomsRepo.save(masSymptoms)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasSymptomsResponse>() {
                        },
                        "Symptom not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSymptomsResponse> findBySymptomId(Long id) {
        try{
            Optional<MasSymptoms> masSymptomsOpt = symptomsRepo.findById(id);
            if (masSymptomsOpt.isPresent()) {
                return ResponseUtils.createSuccessResponse(mapToResponse(masSymptomsOpt.get()), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasSymptomsResponse>() {
                        },
                        "Hospital not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasSymptomsResponse>> getAllSymptoms (int flag){
        List<MasSymptoms> masSymptoms;
        if (flag == 1) {
            masSymptoms = symptomsRepo.findByStatusOrderByLastChgDateDesc("y");
        } else if (flag == 0) {
            masSymptoms = symptomsRepo.findAllByOrderBySymptomsNameAsc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasSymptomsResponse> symptomsResp = masSymptoms.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(symptomsResp, new TypeReference<>() {});
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private MasSymptomsResponse mapToResponse (MasSymptoms symptoms){
        MasSymptomsResponse dto = new MasSymptomsResponse();
        dto.setId(symptoms.getId());
        dto.setSymptomsCode(symptoms.getSymptomsCode());
        dto.setSymptomsName(symptoms.getSymptomsName());
        dto.setStatus(symptoms.getStatus());
        dto.setLastChgBy(symptoms.getLastChgBy());
        dto.setLastChgDate(symptoms.getLastChgDate());
        dto.setMostCommonUse(symptoms.getMostCommonUse());
        return dto;
    }
}

