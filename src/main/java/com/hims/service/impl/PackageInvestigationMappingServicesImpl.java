package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.DgInvestigationPackageRepository;
import com.hims.entity.repository.PackageInvestigationMappingRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.PackageInvestigationMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PackageInvestigationMappingDTO;
import com.hims.service.PackageInvestigationMappingService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageInvestigationMappingServicesImpl implements PackageInvestigationMappingService {

    private static final Logger log = LoggerFactory.getLogger(PackageInvestigationMappingServicesImpl.class);

    @Autowired
    PackageInvestigationMappingRepository mapRepo;

    @Autowired
    DgInvestigationPackageRepository packRepo;

    @Autowired
    UserRepo userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private PackageInvestigationMappingDTO toResponse(PackageInvestigationMapping packim) {
        PackageInvestigationMappingDTO dto = new PackageInvestigationMappingDTO();
        dto.setPimId(packim.getPimId());
        dto.setPackageId(packim.getPackageId().getPackId());
        dto.setStatus(packim.getStatus());
        dto.setCreatedBy(packim.getCreatedBy());
        dto.setCreatedOn(packim.getCreatedOn());
        dto.setUpdatedBy(packim.getUpdatedBy());
        dto.setUpdatedOn(packim.getUpdatedOn());

        return dto;
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> createPackMap(PackageInvestigationMappingRequest mapRequest){
        try{
            Optional<DgInvestigationPackage> investigationPackage = packRepo.findById(mapRequest.getPackageId());

            if (investigationPackage.isPresent()) {
                PackageInvestigationMapping packMap = new PackageInvestigationMapping();
                packMap.setPackageId(investigationPackage.get());
                packMap.setStatus("y");
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                packMap.setCreatedBy(String.valueOf(currentUser.getUserId()));
                packMap.setCreatedOn(LocalDateTime.now());
                packMap.setUpdatedBy(null);
                packMap.setUpdatedOn(null);
                return ResponseUtils.createSuccessResponse(toResponse(mapRepo.save(packMap)), new TypeReference<>() {
                });
            } else {

                return ResponseUtils.createNotFoundResponse("Package not found with Id", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> updatePackMap(Long pimId, PackageInvestigationMappingRequest mapRequest){
        try{
            Optional<PackageInvestigationMapping> optionalMap = mapRepo.findById(pimId);
            if (optionalMap.isPresent()) {
                PackageInvestigationMapping packMap = optionalMap.get();
                Optional<DgInvestigationPackage> investigationPackage = packRepo.findById(mapRequest.getPackageId());
                if (investigationPackage.isPresent()) {
                    packMap.setPackageId(investigationPackage.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("Package not found with Id: " + mapRequest.getPackageId(), 404);
                }
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                packMap.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                packMap.setUpdatedOn(LocalDateTime.now());
                return ResponseUtils.createSuccessResponse(toResponse(mapRepo.save(packMap)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Package mapping is not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> changeStatus(Long pimId, String status){
        try{
            Optional<PackageInvestigationMapping> optionalMap = mapRepo.findById(pimId);
            if(optionalMap.isPresent()){
                PackageInvestigationMapping packMap = optionalMap.get();
                if ("Y".equalsIgnoreCase(status)|| "N".equalsIgnoreCase(status)){
                    packMap.setStatus(status);

                    User currentUser = getCurrentUser();
                    if (currentUser == null) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                                },
                                "Current user not found", HttpStatus.UNAUTHORIZED.value());
                    }
                    packMap.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                    packMap.setUpdatedOn(LocalDateTime.now());

                    return ResponseUtils.createSuccessResponse(toResponse(mapRepo.save(packMap)), new TypeReference<>() {});

                }else{
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, "Invalid status. Status should be 'Y' or 'N'", 400);
                }
            }else{
                return ResponseUtils.createNotFoundResponse("MasItemType is not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> getByPimId(Long pimId){
        Optional <PackageInvestigationMapping> packMap = mapRepo.findById(pimId);
        if(packMap.isPresent()){
            PackageInvestigationMapping newMap= packMap.get();
            return ResponseUtils.createSuccessResponse(toResponse(newMap), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "SubCharge is not found", 404);
        }
    }

    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> getAllPackageMap(int flag){
        List<PackageInvestigationMapping> packMap;
        if (flag == 1) {
            packMap = mapRepo.findByStatus("y");
        } else if (flag == 0) {
            packMap = mapRepo.findAll();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<PackageInvestigationMappingDTO> dto = packMap.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(dto, new TypeReference<>() {});
    }

}
