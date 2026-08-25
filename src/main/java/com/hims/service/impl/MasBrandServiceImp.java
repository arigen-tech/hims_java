package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasBrand;
import com.hims.entity.MasHSN;
import com.hims.entity.User;
import com.hims.entity.repository.MasBrandRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.exception.SDDException;
import com.hims.request.MasBrandRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBrandResponse;
import com.hims.response.MasHsnResponse;
import com.hims.service.MasBrandService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${drugSectionCode}")
    private String drugSectionCode;

    @Value("${nonDrugSectionCode}")
    private String nonDrugSectionCode;

    @Value("${medicalConsumableItemTypeCode}")
    private String medicalConsumableItemTypeCode;

    @Value("${medicalNonConsumableItemTypeCode}")
    private String medicalNonConsumableItemTypeCode;

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
                brands = masBrandRepository.findAllByOrderByStatusDescLastUpdatedDtDesc();
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

    @Override
    public ApiResponse<List<MasBrandResponse>> getBrandsWrtManufacturerAndItemTypeCode(Long manufacturerId, String itemTypeCode) {
        try {
            log.info("getBrandsWrtManufacturerAndItemTypeCode method started ...");
            if(drugSectionCode.equalsIgnoreCase(itemTypeCode)){
                itemTypeCode=medicalConsumableItemTypeCode;
            }else if(nonDrugSectionCode.equalsIgnoreCase(itemTypeCode)){
                itemTypeCode=medicalNonConsumableItemTypeCode;
            }

            Optional<List<MasBrandResponse>>  brandsOpt=masBrandRepository.getBrandsWrtManufacturerAndItemTypeCode(manufacturerId,
                    itemTypeCode,
                    AppConstants.STATUS_Y.toLowerCase()
            );
            if(brandsOpt.isEmpty()){
                throw  new SDDException("brands",
                        HttpStatus.NOT_FOUND.value(),
                        "No brands available for given Manufacturer"
                );
            }

            log.info("getBrandsWrtManufacturerAndItemTypeCode method ended ...");
            return ResponseUtils.createSuccessResponse(brandsOpt.get(), new TypeReference<>() {});
        }catch (SDDException e){
            return  ResponseUtils.createNotFoundResponse(e.getMessage(),e.getStatus());
        }catch (Exception e){
            log.error("getBrandsWrtManufacturerAndItemTypeCode method error :: ",e);
            return  ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
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
