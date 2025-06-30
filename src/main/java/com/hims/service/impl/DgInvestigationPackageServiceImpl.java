package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgInvestigationPackage;
import com.hims.entity.User;
import com.hims.entity.repository.DgInvestigationPackageRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.DgInvestigationPackageRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgInvestigationPackageDTO;
import com.hims.response.DgInvestigationPackageResponse;
import com.hims.service.DgInvestigationPackageServices;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class DgInvestigationPackageServiceImpl implements DgInvestigationPackageServices {

    private static final Logger log = LoggerFactory.getLogger(DgInvestigationPackageServiceImpl.class);

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

    private DgInvestigationPackageDTO toResponse(DgInvestigationPackage pack) {
        DgInvestigationPackageDTO dto = new DgInvestigationPackageDTO();
        dto.setPackId(pack.getPackId());
        dto.setPackName(pack.getPackName());
        dto.setDescrp(pack.getDescrp());
        dto.setBaseCost(pack.getBaseCost());
        dto.setDisc(pack.getDisc());
        dto.setDiscPer(pack.getDiscPer());
        dto.setActualCost(pack.getActualCost());
        dto.setStatus(pack.getStatus());
        dto.setCreatedBy(pack.getCreatedBy());
        dto.setCreatedDt(pack.getCreatedDt());
        dto.setUpdatedBy(pack.getUpdatedBy());
        dto.setUpdatedDt(pack.getUpdatedDt());
        dto.setFromDt(pack.getFromDt());
        dto.setToDt(pack.getToDt());
        dto.setCategory(pack.getCategory());
        dto.setDiscFlag(pack.getDiscFlag());

        return dto;
    }

    @Override
    public ApiResponse<DgInvestigationPackageDTO> createInvestPack(DgInvestigationPackageRequest packReq) {
        try {
            DgInvestigationPackage pack = new DgInvestigationPackage();
            pack.setPackName(packReq.getPackName());
            pack.setDescrp(packReq.getDescrp());
            pack.setBaseCost(packReq.getBaseCost());
            pack.setDisc(packReq.getDisc());
            pack.setDiscPer(packReq.getDiscPer());
            pack.setActualCost(packReq.getActualCost());
            pack.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            pack.setCreatedBy(String.valueOf(currentUser.getUserId()));
            pack.setCreatedDt(LocalDateTime.now());
            pack.setUpdatedBy(null);
            pack.setUpdatedDt(null);
            pack.setFromDt(packReq.getFromDt());
            pack.setToDt(packReq.getToDt());
            pack.setCategory(packReq.getCategory());
            pack.setDiscFlag(packReq.getDiscFlag());
            return ResponseUtils.createSuccessResponse(toResponse(packRepo.save(pack)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgInvestigationPackageDTO> updateInvestPack(Long packId, DgInvestigationPackageRequest packReq) {
        try {
            Optional<DgInvestigationPackage> optionalPack = packRepo.findById(packId);

            if (optionalPack.isPresent()) {
                DgInvestigationPackage pack = optionalPack.get();
                pack.setPackName(packReq.getPackName());
                pack.setDescrp(packReq.getDescrp());
                pack.setBaseCost(packReq.getBaseCost());
                pack.setDisc(packReq.getDisc());
                pack.setDiscPer(packReq.getDiscPer());
                pack.setActualCost(packReq.getActualCost());

                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                pack.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                pack.setUpdatedDt(LocalDateTime.now());
                pack.setFromDt(packReq.getFromDt());
                pack.setToDt(packReq.getToDt());
                pack.setCategory(packReq.getCategory());
                pack.setDiscFlag(packReq.getDiscFlag());
                return ResponseUtils.createSuccessResponse(toResponse(packRepo.save(pack)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Investigation Data not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgInvestigationPackageDTO> changeStatus(Long packId, String status) {
        try {
            Optional<DgInvestigationPackage> newPack = packRepo.findById(packId);

            if (newPack.isPresent()) {
                DgInvestigationPackage pack = newPack.get();
                if ("Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status)) {
                    pack.setStatus(status);

                    User currentUser = getCurrentUser();
                    if (currentUser == null) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                                },
                                "Current user not found", HttpStatus.UNAUTHORIZED.value());
                    }
                    pack.setUpdatedBy(String.valueOf(currentUser.getUserId()));
                    pack.setUpdatedDt(LocalDateTime.now());

                    return ResponseUtils.createSuccessResponse(toResponse(packRepo.save(pack)), new TypeReference<>() {
                    });

                } else {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, "Invalid status. Status should be 'Y' or 'N'", 400);
                }
            } else {
                return ResponseUtils.createNotFoundResponse("MainCharge is not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgInvestigationPackageDTO> getByPackId(Long packId) {
        Optional<DgInvestigationPackage> pack = packRepo.findById(packId);
        if (pack.isPresent()) {
            DgInvestigationPackage newPack = pack.get();
            return ResponseUtils.createSuccessResponse(toResponse(newPack), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "SubCharge is not found", 404);
        }
    }

    @Override
    public ApiResponse<List<DgInvestigationPackageDTO>> getAllPackInvestigation(int flag) {
        List<DgInvestigationPackage> pack;
        if (flag == 1) {
            pack = packRepo.findByStatus("y");
        } else if (flag == 0) {
            pack = packRepo.findAll();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<DgInvestigationPackageDTO> packDTO = pack.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(packDTO, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<DgInvestigationPackageResponse> getPrice(String packName) {
        LocalDate today = LocalDate.now();
        List<Object[]> results = packRepo.findActivePackageByNameAndDateRaw(packName, today);

        if (!results.isEmpty()) {
            Object[] row = results.get(0);

            DgInvestigationPackageResponse response = new DgInvestigationPackageResponse(
                    ((Number) row[0]).longValue(),   // id
                    (String) row[1],                 // name
                    ((Number) row[2]).doubleValue(), // actual_cost
                    (String) row[3]                  // category
            );

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "packName not found", 404);
        }
}}