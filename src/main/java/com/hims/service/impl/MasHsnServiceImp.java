package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasGender;
import com.hims.entity.MasHSN;
import com.hims.entity.User;
import com.hims.entity.repository.MasHsnRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasHsnRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.response.MasGenderResponse;
import com.hims.response.MasHsnResponse;
import com.hims.service.MasHsnService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasHsnServiceImp implements MasHsnService {
    private static final Logger log = LoggerFactory.getLogger(MasGenderServiceImpl.class);
    @Autowired
    UserRepo userRepo;
    @Autowired
    private MasHsnRepository masHsnRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    @Override
    public ApiResponse<List<MasHsnResponse>> getAllMasStoreItem(int flag) {
        List<MasHSN> masHSN;

        if (flag == 1) {
            masHSN = masHsnRepository.findByStatusIgnoreCase("y");
        } else if (flag == 0) {
            masHSN = masHsnRepository.findAllByOrderByStatusDescLastUpdatedDtDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasHsnResponse> responses = masHSN.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });


    }

    @Override
    public ApiResponse<MasHsnResponse> addMasHSN(MasHsnRequest masHsnRequest) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            MasHSN masHsn = new MasHSN();
            masHsn.setCreatedBy(currentUser.getUsername());
            masHsn.setHsnCode(masHsnRequest.getHsnCode());
            masHsn.setGstRate(masHsnRequest.getGstRate());
            masHsn.setIsMedicine(masHsnRequest.getIsMedicine());
            masHsn.setHsnCategory(masHsnRequest.getHsnCategory());
            masHsn.setHsnSubcategory(masHsnRequest.getHsnSubcategory());
            masHsn.setEffectiveFrom(masHsnRequest.getEffectiveFrom());
            masHsn.setEffectiveTo(masHsnRequest.getEffectiveTo());
            masHsn.setStatus("y");
            masHsn.setLastUpdatedDt(LocalDateTime.now());
            return ResponseUtils.createSuccessResponse(mapToResponse(masHsnRepository.save(masHsn)), new TypeReference<>() {
            });
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasHsnResponse> findById(String id) {
        Optional<MasHSN> masHSN = masHsnRepository.findById(id);
        if (masHSN.isPresent()) {
            return ResponseUtils.createSuccessResponse(mapToResponse(masHSN.get()), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasHsnResponse>() {
            }, "MasHSN not found", 404);
        }

    }

    @Override
    public ApiResponse<MasHsnResponse> changeMasHsnStatus(String id, String status) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            if (!"y".equalsIgnoreCase(status) && !"n".equalsIgnoreCase(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status value. Use 'y' or 'n'.", HttpStatus.BAD_REQUEST.value());
            }

            Optional<MasHSN> optionalMasHsn = masHsnRepository.findById(id);
            if (optionalMasHsn.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasHSN not found", HttpStatus.NOT_FOUND.value());
            }

            MasHSN masHsn = optionalMasHsn.get();
            masHsn.setStatus(status.toLowerCase());
            masHsn.setCreatedBy(currentUser.getUsername());
            masHsn.setLastUpdatedDt(LocalDateTime.now());
            MasHSN updatedEntity = masHsnRepository.save(masHsn); // Save the change

            MasHsnResponse response = mapToResponse(updatedEntity); // Convert entity to response
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {}
                    );

        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasHsnResponse> update(String id, MasHsnRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            Optional<MasHSN> optionalMasHsn = masHsnRepository.findById(id);
            if (optionalMasHsn.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasHsnResponse>() {},
                        "MasHSN not found", HttpStatus.NOT_FOUND.value());
            }

            MasHSN masHsn = optionalMasHsn.get();
            masHsn.setHsnCategory(request.getHsnCategory());
            masHsn.setHsnSubcategory(request.getHsnSubcategory());
            masHsn.setEffectiveFrom(request.getEffectiveFrom());
            masHsn.setEffectiveTo(request.getEffectiveTo());
            masHsn.setGstRate(request.getGstRate());
            masHsn.setIsMedicine(request.getIsMedicine());
            masHsn.setCreatedBy(currentUser.getUsername());
            masHsn.setLastUpdatedDt(LocalDateTime.now());
            MasHSN updatedEntity = masHsnRepository.save(masHsn);
            MasHsnResponse response = mapToResponse(updatedEntity);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasHsnResponse mapToResponse(MasHSN masHSN){
        MasHsnResponse response=new MasHsnResponse();
        response.setCreatedBy(masHSN.getCreatedBy());
        response.setHsnCode(masHSN.getHsnCode());
        response.setGstRate(masHSN.getGstRate());
        response.setIsMedicine(masHSN.getIsMedicine());
        response.setHsnCategory(masHSN.getHsnCategory());
        response.setHsnSubcategory(masHSN.getHsnSubcategory());
        response.setEffectiveFrom(masHSN.getEffectiveFrom());
        response.setEffectiveTo(masHSN.getEffectiveTo());
        response.setStatus(masHSN.getStatus());
        response.setLastUpdatedDt(LocalDateTime.now());
        return response;
    }
}
