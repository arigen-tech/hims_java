package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.service.DgMasInvestigationService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DgMasInvestigationServiceImpl implements DgMasInvestigationService {

    private static final Logger log = LoggerFactory.getLogger(DgMasInvestigationServiceImpl.class);

    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepo;

    @Autowired
    private MasMainChargeCodeRepository mainChargeCodeRepo;

    @Autowired
    private MasSubChargeCodeRepository subChargeCodeRepo;

    @Autowired
    private DgMasSampleRepository sampleRepo;

    @Autowired
    private DgUomRepository uomRepo;

    @Autowired
    private DgMasCollectionRepository collectionRepo;

    @Autowired
    private UserRepo userRepo;

    @Value("${investigation.mainChargecodeId}")
    private Long mainChargecodeId;


    @Override
    public ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable) {
        List<Object[]> results = dgMasInvestigationRepo.findByPriceDetails(genderApplicable, mainChargecodeId);

        List<DgMasInvestigationResponse> response = results.stream().map(obj -> {
            DgMasInvestigationResponse dto = new DgMasInvestigationResponse();
            dto.setInvestigationId(obj[0] != null ? ((Number) obj[0]).longValue() : null);
            dto.setInvestigationName((String) obj[1]);
            dto.setStatus(obj[2] != null ? obj[2].toString() : null);
            dto.setGenderApplicable(obj[3] != null ? obj[3].toString() : null);
            dto.setPrice(obj[4] != null ? ((Number) obj[4]).doubleValue() : 0.0);
            dto.setMainChargeCodeId(obj[5] != null ? ((Number) obj[5]).longValue() : null);
            return dto;
        }).collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag) {
        try {
            List<DgMasInvestigation> investigationList;
            if (flag == 1) {
                investigationList = dgMasInvestigationRepo.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                investigationList = dgMasInvestigationRepo.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all, 1 for active.",
                        400
                );
            }

            if (investigationList == null) {
                investigationList = new ArrayList<>();
            }

            List<DgMasInvestigationResponse> responseList = investigationList.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error retrieving investigations: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<DgMasInvestigationResponse> createInvestigation(DgMasInvestigationRequest investigationRequest) {
        try{
            DgMasInvestigation masInvestigation = new DgMasInvestigation();
            masInvestigation.setInvestigationName(investigationRequest.getInvestigationName());
            masInvestigation.setStatus("y");
            masInvestigation.setConfidential(investigationRequest.getConfidential());
            masInvestigation.setAppearInDischargeSummary(investigationRequest.getAppearInDischargeSummary());
            masInvestigation.setInvestigationType(investigationRequest.getInvestigationType());
            masInvestigation.setMultipleResults(investigationRequest.getMultipleResults());
            masInvestigation.setQuantity(investigationRequest.getQuantity());
            masInvestigation.setNormalValue(investigationRequest.getNormalValue());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            masInvestigation.setLastChgBy(currentUser.getUsername());
            masInvestigation.setLastChgDate(Instant.now());
            masInvestigation.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            masInvestigation.setAppointmentRequired(investigationRequest.getAppointmentRequired());
            masInvestigation.setMaxNormalValue(investigationRequest.getMaxNormalValue());
            masInvestigation.setMinNormalValue(investigationRequest.getMinNormalValue());
            masInvestigation.setTestOrderNo(investigationRequest.getTestOrderNo());
            masInvestigation.setNumericOrString(investigationRequest.getNumericOrString());
            masInvestigation.setHicCode(investigationRequest.getHicCode());
            Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(investigationRequest.getMainChargeCodeId());
            masInvestigation.setMainChargeCodeId(mmcc.get());
            Optional<DgUom> du = uomRepo.findById(investigationRequest.getUomId());
            masInvestigation.setUomId(du.get());
            Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(investigationRequest.getSubChargeCodeId());
            masInvestigation.setSubChargeCodeId(mscc.get());
            Optional<DgMasSample> dms = sampleRepo.findById(investigationRequest.getSampleId());
            masInvestigation.setSampleId(dms.get());
            masInvestigation.setEquipmentId(investigationRequest.getEquipmentId());
            Optional<DgMasCollection> dmc = collectionRepo.findById(investigationRequest.getCollectionId());
            masInvestigation.setCollectionId(dmc.get());
            masInvestigation.setBloodReactionTest(investigationRequest.getBloodReactionTest());
            masInvestigation.setBloodBankScreenTest(investigationRequest.getBloodBankScreenTest());
            masInvestigation.setInstructions(investigationRequest.getInstructions());
            masInvestigation.setDiscountApplicable(investigationRequest.getDiscountApplicable());
            masInvestigation.setGenderApplicable(masInvestigation.getGenderApplicable());
            masInvestigation.setDiscount(masInvestigation.getDiscount());
            masInvestigation.setPrice(masInvestigation.getPrice());
            return ResponseUtils.createSuccessResponse(mapToResponse(dgMasInvestigationRepo.save(masInvestigation)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private DgMasInvestigationResponse mapToResponse(DgMasInvestigation entity) {
        DgMasInvestigationResponse dto = new DgMasInvestigationResponse();
        dto.setInvestigationId(entity.getInvestigationId());
        dto.setInvestigationName(entity.getInvestigationName());
        dto.setStatus(entity.getStatus());
        dto.setConfidential(entity.getConfidential());
        dto.setAppearInDischargeSummary(entity.getAppearInDischargeSummary());
        dto.setInvestigationType(entity.getInvestigationType());
        dto.setMultipleResults(entity.getMultipleResults());
        dto.setQuantity(entity.getQuantity());
        dto.setNormalValue(entity.getNormalValue());
        dto.setLastChgBy(entity.getLastChgBy());
        dto.setLastChgTime(entity.getLastChgTime());
        dto.setLastChgDate(entity.getLastChgDate());
        dto.setAppointmentRequired(entity.getAppointmentRequired());
        dto.setMaxNormalValue(entity.getMaxNormalValue());
        dto.setMinNormalValue(entity.getMinNormalValue());
        dto.setTestOrderNo(entity.getTestOrderNo());
        dto.setNumericOrString(entity.getNumericOrString());
        dto.setHicCode(entity.getHicCode());
        dto.setMainChargeCodeId(entity.getMainChargeCodeId() != null ? entity.getMainChargeCodeId().getChargecodeId() : null);
        dto.setMainChargeCodeName(entity.getMainChargeCodeId() != null ? entity.getMainChargeCodeId().getChargecodeName() : null);
        dto.setUomId(entity.getUomId() != null ? entity.getUomId().getId() : null);
        dto.setUomName(entity.getUomId() != null ? entity.getUomId().getName() : null);
        dto.setSubChargeCodeId(entity.getSubChargeCodeId() != null ? entity.getSubChargeCodeId().getSubId() : null);
        dto.setSubChargeCodeName(entity.getSubChargeCodeId() != null ? entity.getSubChargeCodeId().getSubName() : null);
        dto.setSampleId(entity.getSampleId() != null ? entity.getSampleId().getId() : null);
        dto.setSampleName(entity.getSampleId() != null ? entity.getSampleId().getSampleDescription() : null);
        dto.setEquipmentId(entity.getEquipmentId());
        dto.setCollectionId(entity.getCollectionId() != null ? entity.getCollectionId().getCollectionId() : null);
        dto.setCollectionName(entity.getCollectionId() != null ? entity.getCollectionId().getCollectionName() : null);
        dto.setBloodReactionTest(entity.getBloodReactionTest());
        dto.setBloodBankScreenTest(entity.getBloodBankScreenTest());
        dto.setInstructions(entity.getInstructions());
        dto.setDiscountApplicable(entity.getDiscountApplicable());
        dto.setGenderApplicable(entity.getGenderApplicable());
        dto.setDiscount(entity.getDiscount());
        dto.setPrice(entity.getPrice());
        return dto;
    }

}
