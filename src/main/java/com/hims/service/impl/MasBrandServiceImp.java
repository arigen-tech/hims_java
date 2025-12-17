package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBrand;
import com.hims.entity.MasHSN;
import com.hims.entity.User;
import com.hims.entity.repository.MasBrandRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasBrandRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBrandResponse;
import com.hims.response.MasHsnResponse;
import com.hims.service.MasBrandService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasBrandServiceImp implements MasBrandService {
    @Autowired
    private MasBrandRepository masBrandRepository;
    private static final Logger log = LoggerFactory.getLogger(MasCountryServiceImpl.class);

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
    public ApiResponse<List<MasBrandResponse>> getAllMasBrand(int flag) {
            List<MasBrand> brands;
            if (flag == 1) {
                brands = masBrandRepository.findByStatusIgnoreCaseOrderByBrandNameAsc("y");
            } else if (flag == 0) {
                brands = masBrandRepository.findByStatusIgnoreCaseInOrderByLastUpdatedDtDesc(List.of("y", "n"));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid flag", 400);
            }

            List<MasBrandResponse> responses = brands.stream().map(this::mapToResponse).collect(Collectors.toList());
            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });
        }


    @Override
    public ApiResponse<MasBrandResponse> addMasBrand(MasBrandRequest masBrandRequest) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "User not found", 401);
        }

        MasBrand brand = new MasBrand();
        brand.setBrandName(masBrandRequest.getBrandName());
        brand.setDescription(masBrandRequest.getDescription());
        brand.setStatus("y");
        brand.setLastUpdatedBy(user.getUsername());
        brand.setLastUpdatedDt(LocalDateTime.now());
        return ResponseUtils.createSuccessResponse(mapToResponse(masBrandRepository.save(brand)), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasBrandResponse> update(Long id, MasBrandRequest request) {
        Optional<MasBrand> optionalBrand = masBrandRepository.findById(id);
        if (optionalBrand.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Brand not found", 404);
        }

        User user = getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "User not found", 401);
        }

        MasBrand brand = optionalBrand.get();
        brand.setBrandName(request.getBrandName());
        brand.setDescription(request.getDescription());
        brand.setLastUpdatedBy(user.getUsername());
        brand.setLastUpdatedDt(LocalDateTime.now());
        return ResponseUtils.createSuccessResponse(mapToResponse(masBrandRepository.save(brand)), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasBrandResponse> findById(Long id) {
        return masBrandRepository.findById(id)
                .map(brand -> ResponseUtils.createSuccessResponse(mapToResponse(brand), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Brand not found", 404));
    }

    @Override
    public ApiResponse<MasBrandResponse> changeMasBrandStatus(Long id, String status) {
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

            Optional<MasBrand> optionalMasHsn = masBrandRepository.findById(id);
            if (optionalMasHsn.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasBrand not found", HttpStatus.NOT_FOUND.value());
            }

            MasBrand masBrand = optionalMasHsn.get();
            masBrand.setStatus(status.toLowerCase());
            masBrand.setLastUpdatedBy(currentUser.getUsername());
            masBrand.setLastUpdatedDt(LocalDateTime.now());
            MasBrand updatedEntity = masBrandRepository.save(masBrand);

            MasBrandResponse response = mapToResponse(updatedEntity);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {}
            );

        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    private MasBrandResponse mapToResponse(MasBrand brand) {
        MasBrandResponse response = new MasBrandResponse();
        response.setBrandId(brand.getBrandId());
        response.setBrandName(brand.getBrandName());
        response.setDescription(brand.getDescription());
        response.setStatus(brand.getStatus());
        response.setLastUpdatedBy(brand.getLastUpdatedBy());
        response.setLastUpdatedDt(brand.getLastUpdatedDt());
        return response;
    }
}
