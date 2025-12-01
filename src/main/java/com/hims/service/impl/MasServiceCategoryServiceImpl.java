package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasServiceCategory;
import com.hims.entity.User;
import com.hims.entity.repository.MasServiceCategoryRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.response.ApiResponse;
import com.hims.response.GstConfigResponse;
import com.hims.service.MasServiceCategoryService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MasServiceCategoryServiceImpl implements MasServiceCategoryService {
    private static final Logger log = LoggerFactory.getLogger(MasServiceCategoryServiceImpl.class);
    @Autowired
    private final MasServiceCategoryRepository masServiceCategoryRepository;
    @Autowired
    UserRepo userRepo;

    @Value("${serviceCategoryLab}")
    private String serviceCategoryLabCode;

    @Value("${serviceCategoryOPD}")
    private String serviceCategoryOpdCode;

    public MasServiceCategoryServiceImpl(MasServiceCategoryRepository masServiceCategoryRepository) {
        this.masServiceCategoryRepository = masServiceCategoryRepository;
    }

    @Override
    public ApiResponse<List<MasServiceCategory>> findAll(int flag) {
        List<MasServiceCategory> response;
        if(flag==1){

            response = masServiceCategoryRepository.findAllByStatus("y");
        }else{
            response = masServiceCategoryRepository.findAll();
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
    }
    @Override
    public ApiResponse<MasServiceCategory> save(MasServiceCategory req) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            if (req.getStatus() == null || req.getStatus().isBlank()) {
                req.setStatus("y");
            }

            req.setLastChgBy(currentUser.getUsername());
            req.setLastChgDt(Instant.now());

            if (req.getServiceCateCode() == null || req.getServiceCateCode().isBlank()) {
                String lastServiceCateCode = masServiceCategoryRepository.findTopServiceCateCode();
                int nextNumber = 1;
                if (lastServiceCateCode != null && lastServiceCateCode.startsWith("SRV")) {
                    String numberPart = lastServiceCateCode.substring(4);
                    try {
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    } catch (NumberFormatException ignored) {}
                }
                String nextServiceCateCode = String.format("SRV%04d", nextNumber);
                req.setServiceCateCode(nextServiceCateCode);
            }

            MasServiceCategory response = masServiceCategoryRepository.save(req);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(req, new TypeReference<>() {}, "Error Saving Data", 500);
        }
    }

    @Override
    public ApiResponse<MasServiceCategory> edit(Long id, MasServiceCategory req) {
        try {
            Optional<MasServiceCategory> optional = masServiceCategoryRepository.findById(id);

            if (optional.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Service category not found with ID: " + id, HttpStatus.NOT_FOUND.value());
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            MasServiceCategory existing = optional.get();

            // Update only necessary fields
            existing.setServiceCatName(req.getServiceCatName());
            existing.setSacCode(req.getSacCode());
            existing.setGstApplicable(req.getGstApplicable());
            existing.setStatus(req.getStatus() != null ? req.getStatus() : existing.getStatus());

            existing.setLastChgBy(currentUser.getUsername());
            existing.setLastChgDt(Instant.now());

            MasServiceCategory updated = masServiceCategoryRepository.save(existing);

            return ResponseUtils.createSuccessResponse(updated, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(req, new TypeReference<>() {}, "Error Updating Data", 500);
        }
    }

    @Override
    public ApiResponse<MasServiceCategory> updateStatus(Long id, String status) {
        try {
            Optional<MasServiceCategory> optional = masServiceCategoryRepository.findById(id);

            if (optional.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Service category not found with ID: " + id, HttpStatus.NOT_FOUND.value());
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            MasServiceCategory entity = optional.get();
            entity.setStatus(status);
            entity.setLastChgBy(currentUser.getUsername());
            entity.setLastChgDt(Instant.now());

            MasServiceCategory updated = masServiceCategoryRepository.save(entity);
            return ResponseUtils.createSuccessResponse(updated, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Error updating status", 500);
        }
    }


    @Override
    public ApiResponse<GstConfigResponse> getGstConfig(int flag , Integer catId) {
        MasServiceCategory category;
        if(catId!=null){
             category = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryOpdCode);
        }else{
             category = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLabCode);
        }

        ApiResponse<GstConfigResponse> apiResponse = new ApiResponse<>();

        if (category != null) {
            GstConfigResponse gstConfig = new GstConfigResponse(
                    category.getGstApplicable(),
                    category.getGstPercent()
            );

            apiResponse.setStatus(HttpStatus.OK.value());
            apiResponse.setMessage("GST config fetched successfully");
            apiResponse.setResponse(gstConfig);

        } else {
            apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
            apiResponse.setMessage("Service category not found for code: " + serviceCategoryLabCode);
            apiResponse.setResponse(null);
        }

        return apiResponse;
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);

        }
        return user;
    }
}
