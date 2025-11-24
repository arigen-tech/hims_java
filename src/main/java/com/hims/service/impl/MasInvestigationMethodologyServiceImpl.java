package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.MasInvestigationCategory;
import com.hims.entity.MasInvestigationMethodology;
import com.hims.entity.User;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.entity.repository.MasInvestigationMethodologyRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasInvestigationMethodologyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationCategoryResponse;
import com.hims.response.MasInvestigationMethodologyResponse;
import com.hims.service.MasInvestigationMethodologyService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MasInvestigationMethodologyServiceImpl implements MasInvestigationMethodologyService {
    @Autowired
    private MasInvestigationMethodologyRepository masInvestigationMethodologyRepository;
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;
  private static final Logger log = LoggerFactory.getLogger( MasInvestigationMethodologyServiceImpl.class);
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
    public ApiResponse create(MasInvestigationMethodologyRequest request) {
        log.info("MasInvestigationMethodology create request received: {}", request);
        try{

            MasInvestigationMethodology masInvestigationMethodology=new MasInvestigationMethodology();
            masInvestigationMethodology.setMethodName(request.getMethodName());
            masInvestigationMethodology.setInvestigation(request.getInvestigationId() != null
                    ? dgMasInvestigationRepository.findByinvestigationId(request.getInvestigationId())
                    : null
            );

            masInvestigationMethodology.setNote(request.getNote());
            masInvestigationMethodology.setLastChgBy(getCurrentUser().getUsername());
            masInvestigationMethodology.setLastChgDate(LocalDate.now());
            masInvestigationMethodologyRepository.save(masInvestigationMethodology);
            log.info("MasInvestigationMethodology created successfully.");
            return ResponseUtils.createSuccessResponse("MasInvestigationMethodologyCreate", new TypeReference<>() {
            });

        }catch(Exception e){
            log.error("Error while creating MasInvestigationMethodology: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasInvestigationMethodologyResponse>> get() {
        try {
            List<MasInvestigationMethodology> masInvestigationCategories =
                    masInvestigationMethodologyRepository.findAll();
            return ResponseUtils.createSuccessResponse(
                    convertList(masInvestigationCategories),
                    new TypeReference<>() {
                    }
            );
        }catch(Exception e){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


    }

    @Override
    public ApiResponse<String> update(Long id, MasInvestigationMethodologyRequest request) {
        log.info("MasInvestigationMethodology create request received: {}", request);
        try {
            Optional<MasInvestigationMethodology> masInvestigationCategory = masInvestigationMethodologyRepository.findById(id);
            if (masInvestigationCategory.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(" masInvestigationMethodology not found", 404);
            }
            MasInvestigationMethodology masInvestigationCategory1 = masInvestigationCategory.get();
            masInvestigationCategory1.setMethodName(request.getMethodName());
            masInvestigationCategory1.setInvestigation(request.getInvestigationId() != null
                    ? dgMasInvestigationRepository.findByinvestigationId(request.getInvestigationId())
                    : null
            );

            masInvestigationCategory1.setLastChgDate(LocalDate.now());
            masInvestigationCategory1.setLastChgBy(getCurrentUser().getUsername());
            masInvestigationCategory1.setNote(request.getNote());
            masInvestigationMethodologyRepository.save(masInvestigationCategory1);
            log.info("MasInvestigationMethodology update successfully.");
            return ResponseUtils.createSuccessResponse("MasInvestigationMethodology Update", new TypeReference<>() {
            });
        }catch(Exception e){
            log.error("Error while creating MasInvestigationMethodology: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


    }

    @Override
    public ApiResponse<MasInvestigationMethodologyResponse> findById(Long id) {
        Optional<MasInvestigationMethodology> masInvestigationCategory = masInvestigationMethodologyRepository.findById(id);
        if (masInvestigationCategory.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(" masInvestigationMethodology not found", 404);
        }
        MasInvestigationMethodology masInvestigationCategory1 = masInvestigationCategory.get();

        return ResponseUtils.createSuccessResponse(
                convertedResponse(masInvestigationCategory1),
                new TypeReference<>() {
                }
        );
    }

    private MasInvestigationMethodologyResponse convertedResponse(MasInvestigationMethodology masInvestigationMethodology){
        MasInvestigationMethodologyResponse masInvestigationCategoryResponse=new MasInvestigationMethodologyResponse();
        masInvestigationCategoryResponse.setMethodId(masInvestigationMethodology.getMethodId());
        masInvestigationCategoryResponse.setMethodName(masInvestigationMethodology.getMethodName());
        masInvestigationCategoryResponse.setNote(masInvestigationMethodology.getNote());
        masInvestigationCategoryResponse.setInvestigationId(
                masInvestigationMethodology.getInvestigation() != null
                        ? masInvestigationMethodology.getInvestigation().getInvestigationId()
                        : null
        );
        return masInvestigationCategoryResponse;
    }
    private List<MasInvestigationMethodologyResponse> convertList(List<MasInvestigationMethodology> list) {
        return list.stream()
                .map(this::convertedResponse)
                .collect(Collectors.toList());
    }
}
