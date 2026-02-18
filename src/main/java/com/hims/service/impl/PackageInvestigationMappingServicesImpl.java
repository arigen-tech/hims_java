package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.DgInvestigationPackageRepository;
import com.hims.entity.repository.DgMasInvestigationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageInvestigationMappingServicesImpl implements PackageInvestigationMappingService {

    private static final Logger log = LoggerFactory.getLogger(PackageInvestigationMappingServicesImpl.class);

    @Autowired
    private PackageInvestigationMappingRepository mapRepo;

    @Autowired
    private DgInvestigationPackageRepository packRepo;

    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;

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

    private PackageInvestigationMappingDTO toResponse(PackageInvestigationMapping packim) {
        PackageInvestigationMappingDTO dto = new PackageInvestigationMappingDTO();
        dto.setPimId(packim.getPimId());
        dto.setPackageId(packim.getPackageId().getPackId());
        dto.setPackName(packim.getPackageId().getPackName());
        dto.setInvestId(packim.getInvestId().getInvestigationId());
        dto.setInvestigationName(packim.getInvestId().getInvestigationName());
        dto.setActualCost(packim.getPackageId().getActualCost());
        dto.setStatus(packim.getStatus());
        dto.setCreatedBy(packim.getCreatedBy());
        dto.setCreatedOn(packim.getCreatedOn());
        dto.setUpdatedBy(packim.getUpdatedBy());
        dto.setUpdatedOn(packim.getUpdatedOn());
        return dto;
    }

    private PackageInvestigationMappingDTO toResponse(DgInvestigationPackage pack) {
        PackageInvestigationMappingDTO dto = new PackageInvestigationMappingDTO();
        dto.setPackageId(pack.getPackId());
        dto.setPackName(pack.getPackName());
        dto.setActualCost(pack.getActualCost());
        dto.setStatus(pack.getStatus());
        dto.setCreatedBy(pack.getCreatedBy());
        dto.setUpdatedBy(pack.getUpdatedBy());
        return dto;
    }

    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> createPackMap(PackageInvestigationMappingRequest request) {
        try {
            DgInvestigationPackage pack = packRepo.findById(request.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            List<PackageInvestigationMappingDTO> dtoList = new ArrayList<>();

            for (Long invId : request.getInvestigationIds()) {
                DgMasInvestigation investigation = dgMasInvestigationRepository.findById(invId)
                        .orElseThrow(() -> new RuntimeException("Investigation not found: " + invId));

                PackageInvestigationMapping map = new PackageInvestigationMapping();
                map.setPackageId(pack);
                map.setInvestId(investigation);
                map.setStatus("y");
                map.setCreatedBy(String.valueOf(currentUser.getUserId()));
                map.setCreatedOn(LocalDateTime.now());

                mapRepo.save(map);
                dtoList.add(toResponse(map));
            }

            return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> getAllMappings(int flag) {
        String status = null;
        if (flag == 1) {
            status = "Y";
        } else if (flag != 0) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Invalid flag value. Use 0 for all, 1 for active only.", 400);
        }

        List<PackageInvestigationMapping> mappings;
        if (status == null) {
            mappings = mapRepo.findAll();
        } else {
            mappings = mapRepo.findByStatus(status);
        }

        List<PackageInvestigationMappingDTO> dtoList = mappings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> updatePackMap(Long pimId, PackageInvestigationMappingRequest mapRequest) {
        try {
            Optional<PackageInvestigationMapping> optionalMap = mapRepo.findById(pimId);
            if (optionalMap.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Package mapping not found for Id: " + pimId, 404);
            }

            PackageInvestigationMapping packMap = optionalMap.get();

            DgInvestigationPackage investigationPackage = packRepo.findById(mapRequest.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package not found with Id: " + mapRequest.getPackageId()));
            packMap.setPackageId(investigationPackage);

            if (mapRequest.getInvestigationIds() != null && !mapRequest.getInvestigationIds().isEmpty()) {
                Long invId = mapRequest.getInvestigationIds().get(0);
                DgMasInvestigation investigation = dgMasInvestigationRepository.findById(invId)
                        .orElseThrow(() -> new RuntimeException("Investigation not found with Id: " + invId));
                packMap.setInvestId(investigation);
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            packMap.setUpdatedBy(String.valueOf(currentUser.getUserId()));
            packMap.setUpdatedOn(LocalDateTime.now());

            return ResponseUtils.createSuccessResponse(
                    toResponse(mapRepo.save(packMap)), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> changeStatus(Long pimId, String status) {
        try {
            Optional<PackageInvestigationMapping> optionalMap = mapRepo.findById(pimId);
            if (optionalMap.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Package mapping not found", 404);
            }

            PackageInvestigationMapping packMap = optionalMap.get();
            if (!("Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status))) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status. Status should be 'Y' or 'N'", 400);
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            // Get the package ID to update all mappings for this package
            Long packageId = packMap.getPackageId().getPackId();

            // Find all mappings for this package
            List<PackageInvestigationMapping> allPackageMappings = mapRepo.findByPackageIdPackId(packageId);

            // Update status for all mappings of this package
            for (PackageInvestigationMapping mapping : allPackageMappings) {
                mapping.setStatus(status);
                mapping.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                mapping.setUpdatedOn(LocalDateTime.now());
                mapRepo.save(mapping);
            }

            return ResponseUtils.createSuccessResponse(toResponse(packMap), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageInvestigationMappingDTO> getByPimId(Long pimId) {
        Optional<PackageInvestigationMapping> packMap = mapRepo.findById(pimId);
        if (packMap.isPresent()) {
            PackageInvestigationMapping newMap = packMap.get();
            return ResponseUtils.createSuccessResponse(toResponse(newMap), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "SubCharge is not found", 404);
        }
    }

    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> getAllPackageMap(int flag) {
        String status = null;
        if (flag == 1) {
            status = "y";
        } else if (flag != 0) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<DgInvestigationPackage> distinctPackages = mapRepo.findDistinctPackages(status);
        List<PackageInvestigationMappingDTO> dtoList = distinctPackages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> getInvestigationsByPackageId(Long packageId) {
        try {
            List<PackageInvestigationMapping> mappings = mapRepo.findByPackageIdPackIdAndStatus(packageId, "y");
            List<PackageInvestigationMappingDTO> dtoList = mappings.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Updated method to DELETE removed investigations instead of deactivating them
    @Override
    public ApiResponse<List<PackageInvestigationMappingDTO>> updatePackageInvestigations(Long packageId, PackageInvestigationMappingRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            DgInvestigationPackage pack = packRepo.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            // Get existing active mappings for this package
            List<PackageInvestigationMapping> existingMappings = mapRepo.findByPackageIdPackIdAndStatus(packageId, "y");

            // Get current investigation IDs from existing mappings
            List<Long> existingInvestigationIds = existingMappings.stream()
                    .map(mapping -> mapping.getInvestId().getInvestigationId())
                    .collect(Collectors.toList());

            // Get new investigation IDs from request
            List<Long> newInvestigationIds = request.getInvestigationIds();

            log.info("Package ID: {}", packageId);
            log.info("Existing investigation IDs: {}", existingInvestigationIds);
            log.info("New investigation IDs: {}", newInvestigationIds);

            // Find investigations to remove (exist in current but not in new)
            List<Long> investigationsToRemove = existingInvestigationIds.stream()
                    .filter(id -> !newInvestigationIds.contains(id))
                    .collect(Collectors.toList());

            // Find investigations to add (exist in new but not in current)
            List<Long> investigationsToAdd = newInvestigationIds.stream()
                    .filter(id -> !existingInvestigationIds.contains(id))
                    .collect(Collectors.toList());

            log.info("Investigations to remove: {}", investigationsToRemove);
            log.info("Investigations to add: {}", investigationsToAdd);

            // DELETE mappings for removed investigations (instead of deactivating)
            if (!investigationsToRemove.isEmpty()) {
                List<PackageInvestigationMapping> mappingsToDelete = existingMappings.stream()
                        .filter(mapping -> investigationsToRemove.contains(mapping.getInvestId().getInvestigationId()))
                        .collect(Collectors.toList());

                for (PackageInvestigationMapping mapping : mappingsToDelete) {
                    log.info("DELETING mapping with pimId: {} for investigation: {}",
                            mapping.getPimId(), mapping.getInvestId().getInvestigationId());
                    mapRepo.delete(mapping);  // This will completely delete from database
                }
            }

            // Add new mappings for added investigations
            for (Long invId : investigationsToAdd) {
                DgMasInvestigation investigation = dgMasInvestigationRepository.findById(invId)
                        .orElseThrow(() -> new RuntimeException("Investigation not found: " + invId));

                // Check if a mapping already exists (including inactive ones)
                Optional<PackageInvestigationMapping> existingMapping = mapRepo
                        .findByPackageIdPackIdAndInvestIdInvestigationId(packageId, invId);

                PackageInvestigationMapping mapping;
                if (existingMapping.isPresent()) {
                    // Reactivate existing mapping
                    mapping = existingMapping.get();
                    mapping.setStatus("y");
                    mapping.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                    mapping.setUpdatedOn(LocalDateTime.now());
                    log.info("REACTIVATING existing mapping for investigation: {}", invId);
                } else {
                    // Create completely new mapping
                    mapping = new PackageInvestigationMapping();
                    mapping.setPackageId(pack);
                    mapping.setInvestId(investigation);
                    mapping.setStatus("y");
                    mapping.setCreatedBy(String.valueOf(currentUser.getUserId()));
                    mapping.setCreatedOn(LocalDateTime.now());
                    log.info("CREATING new mapping for investigation: {}", invId);
                }

                mapRepo.save(mapping);
            }

            // Get updated mappings to return (only active ones)
            List<PackageInvestigationMapping> updatedMappings = mapRepo.findByPackageIdPackIdAndStatus(packageId, "y");
            List<PackageInvestigationMappingDTO> dtoList = updatedMappings.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            log.info("Final active mappings count: {}", dtoList.size());
            return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error updating package investigations: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
