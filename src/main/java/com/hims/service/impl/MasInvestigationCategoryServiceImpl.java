package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasInvestigationCategory;
import com.hims.entity.User;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.entity.repository.MasInvestigationCategoryRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasInvestigationCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationCategoryResponse;
import com.hims.service.MasInvestigationCategoryService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasInvestigationCategoryServiceImpl implements MasInvestigationCategoryService {
    private static final Logger log = LoggerFactory.getLogger(MasInvestigationCategoryServiceImpl.class);
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;
    @Autowired
    private MasInvestigationCategoryRepository masInvestigationCategoryRepository;
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
    public ApiResponse<String> create(MasInvestigationCategoryRequest request) {

        log.info("MasInvestigationCategory create request received: {}", request);
        try{

            MasInvestigationCategory masInvestigationCategory=new MasInvestigationCategory();
            masInvestigationCategory.setCategoryName(request.getCategoryName());
            masInvestigationCategory.setLastChgBy(getCurrentUser().getUsername());
            masInvestigationCategory.setLastChgDate(LocalDate.now());
            masInvestigationCategory.setInvestigation(
                    request.getInvestigationId() != null
                            ? dgMasInvestigationRepository.findByinvestigationId(request.getInvestigationId())
                            : null
            );
            masInvestigationCategoryRepository.save(masInvestigationCategory);
            log.info("MasInvestigationCategory created successfully.");
            return ResponseUtils.createSuccessResponse("MasInvestigationCategoryCreate", new TypeReference<>() {
            });

        }catch(Exception e){
            log.error("Error while creating MasInvestigationCategory: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<List<MasInvestigationCategoryResponse>> get() {
        try {

            List<MasInvestigationCategory> masInvestigationCategories =
                    masInvestigationCategoryRepository.findAll();
            return ResponseUtils.createSuccessResponse(
                    convertList(masInvestigationCategories),
                    new TypeReference<>() {
                    }
            );
        }catch(Exception e){
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<String> update(Long id, MasInvestigationCategoryRequest request) {
        log.info("MasInvestigationCategory create request received: {}", request);
        try {
            Optional<MasInvestigationCategory> masInvestigationCategory = masInvestigationCategoryRepository.findById(id);
            if (masInvestigationCategory.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(" masInvestigationCategory not found", 404);
            }
            MasInvestigationCategory masInvestigationCategory1 = masInvestigationCategory.get();
            masInvestigationCategory1.setCategoryName(request.getCategoryName());
            masInvestigationCategory1.setInvestigation(request.getInvestigationId() != null
                    ? dgMasInvestigationRepository.findByinvestigationId(request.getInvestigationId())
                    : null
            );

            masInvestigationCategory1.setLastChgDate(LocalDate.now());
            masInvestigationCategory1.setLastChgBy(getCurrentUser().getUsername());
            masInvestigationCategoryRepository.save(masInvestigationCategory1);
            log.info("MasInvestigationCategory update successfully.");
            return ResponseUtils.createSuccessResponse("MasInvestigationCategoryUpdate", new TypeReference<>() {
            });
        }catch(Exception e){
            e.printStackTrace();
            log.error("Error while creating MasInvestigationCategory: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasInvestigationCategoryResponse> findById(Long id) {
        Optional<MasInvestigationCategory> masInvestigationCategory = masInvestigationCategoryRepository.findById(id);
        if (masInvestigationCategory.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(" masInvestigationCategory not found", 404);
        }
        MasInvestigationCategory masInvestigationCategory1 = masInvestigationCategory.get();
        return ResponseUtils.createSuccessResponse(
                convertedResponse(masInvestigationCategory1 ),
                new TypeReference<>() {
                }
        );
    }


    private MasInvestigationCategoryResponse convertedResponse(MasInvestigationCategory masInvestigationCategory){
        MasInvestigationCategoryResponse masInvestigationCategoryResponse=new MasInvestigationCategoryResponse();
        masInvestigationCategoryResponse.setCategoryId(masInvestigationCategory.getCategoryId());
        masInvestigationCategoryResponse.setCategoryName(masInvestigationCategory.getCategoryName());
        masInvestigationCategoryResponse.setInvestigationId(
                masInvestigationCategory.getInvestigation() != null
                        ?  masInvestigationCategory.getInvestigation().getInvestigationId()
                        : null
        );
        return masInvestigationCategoryResponse;
    }
    private List<MasInvestigationCategoryResponse> convertList(List<MasInvestigationCategory> list) {
        return list.stream()
                .map(this::convertedResponse)
                .collect(Collectors.toList());
    }
}
