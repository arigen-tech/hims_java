package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.DgFixedValueRequest;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgNormalValueRequest;
import com.hims.request.DgSubMasInvestigationRequest;
import com.hims.response.*;
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

    @Autowired
    private DgSubMasInvestigationRepository subInvestigationRepo;

    @Autowired
    private DgFixedValueRepository fixedRepo;

    @Autowired
    private DgNormalValueRepository normalRepo;

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
            masInvestigation.setGenderApplicable(investigationRequest.getGenderApplicable());
            masInvestigation.setDiscount(investigationRequest.getDiscount());
            masInvestigation.setPrice(investigationRequest.getPrice());
            return ResponseUtils.createSuccessResponse(mapToResponse(dgMasInvestigationRepo.save(masInvestigation)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Data was not appended properly" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgMasInvestigationResponse> updateSingleInvestigation(Long investigationId, DgMasInvestigationRequest investigationRequest) {
        try{
            Optional<DgMasInvestigation> masInvestigation = dgMasInvestigationRepo.findById(investigationId);
            if (masInvestigation.isPresent()) {
                DgMasInvestigation dmi = masInvestigation.get();
                dmi.setInvestigationName(investigationRequest.getInvestigationName());
                dmi.setConfidential(investigationRequest.getConfidential());
                dmi.setAppearInDischargeSummary(investigationRequest.getAppearInDischargeSummary());
                dmi.setInvestigationType(investigationRequest.getInvestigationType());
                dmi.setMultipleResults(investigationRequest.getMultipleResults());
                dmi.setQuantity(investigationRequest.getQuantity());
                dmi.setNormalValue(investigationRequest.getNormalValue());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                dmi.setLastChgBy(currentUser.getUsername());
                dmi.setLastChgDate(Instant.now());
                dmi.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                dmi.setAppointmentRequired(investigationRequest.getAppointmentRequired());
                dmi.setMaxNormalValue(investigationRequest.getMaxNormalValue());
                dmi.setMinNormalValue(investigationRequest.getMinNormalValue());
                dmi.setTestOrderNo(investigationRequest.getTestOrderNo());
                dmi.setNumericOrString(investigationRequest.getNumericOrString());
                dmi.setHicCode(investigationRequest.getHicCode());
                if (investigationRequest.getMainChargeCodeId() != null) {
                    Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(investigationRequest.getMainChargeCodeId());
                    if (mmcc.isPresent()) {
                        dmi.setMainChargeCodeId(mmcc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
                    }
                }
                if (investigationRequest.getUomId() != null) {
                    Optional<DgUom> du = uomRepo.findById(investigationRequest.getUomId());
                    if (du.isPresent()) {
                        dmi.setUomId(du.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("uomId not found", 404);
                    }
                }
                if (investigationRequest.getSubChargeCodeId() != null) {
                    Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(investigationRequest.getSubChargeCodeId());
                    if (mscc.isPresent()) {
                        dmi.setSubChargeCodeId(mscc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("subChargeCodeId not found", 404);
                    }
                }
                if (investigationRequest.getSampleId() != null) {
                    Optional<DgMasSample> dms = sampleRepo.findById(investigationRequest.getSampleId());
                    if (dms.isPresent()) {
                        dmi.setSampleId(dms.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("sampleId not found", 404);
                    }
                }
                dmi.setEquipmentId(investigationRequest.getEquipmentId());
                if (investigationRequest.getCollectionId() != null) {
                    Optional<DgMasCollection> dmc = collectionRepo.findById(investigationRequest.getCollectionId());
                    if (dmc.isPresent()) {
                        dmi.setCollectionId(dmc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("collectionId not found", 404);
                    }
                }
                dmi.setBloodReactionTest(investigationRequest.getBloodReactionTest());
                dmi.setBloodBankScreenTest(investigationRequest.getBloodBankScreenTest());
                dmi.setInstructions(investigationRequest.getInstructions());
                dmi.setDiscountApplicable(investigationRequest.getDiscountApplicable());
                dmi.setGenderApplicable(investigationRequest.getGenderApplicable());
                dmi.setDiscount(investigationRequest.getDiscount());
                dmi.setPrice(investigationRequest.getPrice());
                return ResponseUtils.createSuccessResponse(mapToResponse(dgMasInvestigationRepo.save(dmi)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("investigationId not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "The data cannot be updated" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgMasInvestigationResponse> updateMultipleInvestigation(Long investigationId, DgMasInvestigationRequest investigationRequest) {
        Optional<DgMasInvestigation> masInvestigation = dgMasInvestigationRepo.findById(investigationId);
        if (masInvestigation.isPresent()) {
            DgMasInvestigation dmi = masInvestigation.get();
            dmi.setInvestigationName(investigationRequest.getInvestigationName());
            dmi.setConfidential(investigationRequest.getConfidential());
            dmi.setAppearInDischargeSummary(investigationRequest.getAppearInDischargeSummary());
            dmi.setInvestigationType(investigationRequest.getInvestigationType());
            dmi.setMultipleResults(investigationRequest.getMultipleResults());
            dmi.setQuantity(investigationRequest.getQuantity());
            dmi.setNormalValue(investigationRequest.getNormalValue());

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            dmi.setLastChgBy(currentUser.getUsername());
            dmi.setLastChgDate(Instant.now());
            dmi.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            dmi.setAppointmentRequired(investigationRequest.getAppointmentRequired());
            dmi.setMaxNormalValue(investigationRequest.getMaxNormalValue());
            dmi.setMinNormalValue(investigationRequest.getMinNormalValue());
            dmi.setTestOrderNo(investigationRequest.getTestOrderNo());
            dmi.setNumericOrString(investigationRequest.getNumericOrString());
            dmi.setHicCode(investigationRequest.getHicCode());

            if (investigationRequest.getMainChargeCodeId() != null) {
                Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(investigationRequest.getMainChargeCodeId());
                if (mmcc.isPresent()) {
                    dmi.setMainChargeCodeId(mmcc.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
                }
            }

            if (investigationRequest.getUomId() != null) {
                Optional<DgUom> du = uomRepo.findById(investigationRequest.getUomId());
                if (du.isPresent()) {
                    dmi.setUomId(du.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("uomId not found", 404);
                }
            }

            if (investigationRequest.getSubChargeCodeId() != null) {
                Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(investigationRequest.getSubChargeCodeId());
                if (mscc.isPresent()) {
                    dmi.setSubChargeCodeId(mscc.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("subChargeCodeId not found", 404);
                }
            }

            if (investigationRequest.getSampleId() != null) {
                Optional<DgMasSample> dms = sampleRepo.findById(investigationRequest.getSampleId());
                if (dms.isPresent()) {
                    dmi.setSampleId(dms.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("sampleId not found", 404);
                }
            }

            dmi.setEquipmentId(investigationRequest.getEquipmentId());

            if (investigationRequest.getCollectionId() != null) {
                Optional<DgMasCollection> dmc = collectionRepo.findById(investigationRequest.getCollectionId());
                if (dmc.isPresent()) {
                    dmi.setCollectionId(dmc.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("collectionId not found", 404);
                }
            }

            dmi.setBloodReactionTest(investigationRequest.getBloodReactionTest());
            dmi.setBloodBankScreenTest(investigationRequest.getBloodBankScreenTest());
            dmi.setInstructions(investigationRequest.getInstructions());
            dmi.setDiscountApplicable(investigationRequest.getDiscountApplicable());
            dmi.setGenderApplicable(investigationRequest.getGenderApplicable());
            dmi.setDiscount(investigationRequest.getDiscount());
            dmi.setPrice(investigationRequest.getPrice());

            // saving the updated masInvestigation
            DgMasInvestigation updatedInvestigation = dgMasInvestigationRepo.save(dmi);

            //Saving DgSubMasInvestigation
            List<DgSubMasInvestigationRequest> subRequests = investigationRequest.getSubMasInvestigationRequestlist();
            List<DgSubMasInvestigation> subList = new ArrayList<>();
            for(DgSubMasInvestigationRequest subRequest : subRequests) {
                DgSubMasInvestigation subInvestigation = new DgSubMasInvestigation();
                subInvestigation.setSubInvestigationCode(subRequest.getSubInvestigationCode());
                subInvestigation.setSubInvestigationName(subRequest.getSubInvestigationName());
                subInvestigation.setOrderNo(subRequest.getOrderNo());
                subInvestigation.setResultType(subRequest.getResultType());
                subInvestigation.setComparisonType(subRequest.getComparisonType());
                Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(subRequest.getMainChargeCodeId());
                subInvestigation.setMainChargeCodeId(mmcc.get());
                Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(subRequest.getSubChargeCodeId());
                subInvestigation.setSubChargeCodeId(mscc.get());
                Optional<DgMasSample> dms = sampleRepo.findById(subRequest.getSampleId());
                subInvestigation.setSampleId(dms.get());
                Optional<DgUom> du = uomRepo.findById(subRequest.getUomId());
                subInvestigation.setUomId(du.get());
                subInvestigation.setInvestigationId(updatedInvestigation);
                subList.add(subInvestigation);
            }

            //saving the updated subInvestigation
            subInvestigationRepo.saveAll(subList);

            //Saving DgFixedValue
            List<DgFixedValueRequest> fixedRequests = investigationRequest.getFixedValueRequestList();
            List<DgFixedValue> fixedList = new ArrayList<>();
            for(DgFixedValueRequest fixedRequest : fixedRequests){
                DgFixedValue fixedValue = new DgFixedValue();
                fixedValue.setFixedValue(fixedRequest.getFixedValue());
                Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(fixedRequest.getSubChargeCodeId());
                fixedValue.setSubChargeCodeId(mscc.get());
                fixedList.add(fixedValue);
            }

            //saving the updated fixedValue
            fixedRepo.saveAll(fixedList);

            //saving DgNormalValue
            List<DgNormalValueRequest> normalRequests = investigationRequest.getNormalValueRequestList();
            List<DgNormalValue> normalList = new ArrayList<>();
            for (DgNormalValueRequest normalRequest : normalRequests){
                DgNormalValue normalValue = new DgNormalValue();
                normalValue.setSex(normalRequest.getSex());
                normalValue.setFromAge(normalRequest.getFromAge());
                normalValue.setToAge(normalRequest.getToAge());
                normalValue.setMinNormalValue(normalRequest.getMinNormalValue());
                normalValue.setMaxNormalValue(normalRequest.getMaxNormalValue());
                normalValue.setNormalValue(normalRequest.getNormalValue());
                Optional<DgSubMasInvestigation> dsmi = subInvestigationRepo.findById(normalRequest.getSubInvestigationId());
                normalValue.setSubInvestigationId(dsmi.get());
                Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(normalRequest.getMainChargeCodeId());
                normalValue.setMainChargeCodeId(mmcc.get());
                normalList.add(normalValue);
            }

            //saving the updated normalValue
            normalRepo.saveAll(normalList);

            // Map to response
            DgMasInvestigationResponse response = mapToResponseMulti(updatedInvestigation, subList, fixedList, normalList);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("investigationId not found", 404);
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

    private DgMasInvestigationResponse mapToResponseMulti(
            DgMasInvestigation masInvest,
            List<DgSubMasInvestigation> subInvestigationList,
            List<DgFixedValue> fixedValList,
            List<DgNormalValue> normalValList) {
        DgMasInvestigationResponse dmir = new DgMasInvestigationResponse();
        dmir.setInvestigationId(masInvest.getInvestigationId());
        dmir.setInvestigationName(masInvest.getInvestigationName());
        dmir.setStatus(masInvest.getStatus());
        dmir.setConfidential(masInvest.getConfidential());
        dmir.setAppearInDischargeSummary(masInvest.getAppearInDischargeSummary());
        dmir.setInvestigationType(masInvest.getInvestigationType());
        dmir.setMultipleResults(masInvest.getMultipleResults());
        dmir.setQuantity(masInvest.getQuantity());
        dmir.setNormalValue(masInvest.getNormalValue());
        dmir.setLastChgBy(masInvest.getLastChgBy());
        dmir.setLastChgTime(masInvest.getLastChgTime());
        dmir.setLastChgDate(masInvest.getLastChgDate());
        dmir.setAppointmentRequired(masInvest.getAppointmentRequired());
        dmir.setMaxNormalValue(masInvest.getMaxNormalValue());
        dmir.setMinNormalValue(masInvest.getMinNormalValue());
        dmir.setTestOrderNo(masInvest.getTestOrderNo());
        dmir.setNumericOrString(masInvest.getNumericOrString());
        dmir.setHicCode(masInvest.getHicCode());

        dmir.setMainChargeCodeId(masInvest.getMainChargeCodeId() != null ? masInvest.getMainChargeCodeId().getChargecodeId() : null);
        dmir.setMainChargeCodeName(masInvest.getMainChargeCodeId() != null ? masInvest.getMainChargeCodeId().getChargecodeName() : null);
        dmir.setUomId(masInvest.getUomId() != null ? masInvest.getUomId().getId() : null);
        dmir.setUomName(masInvest.getUomId() != null ? masInvest.getUomId().getName() : null);
        dmir.setSubChargeCodeId(masInvest.getSubChargeCodeId() != null ? masInvest.getSubChargeCodeId().getSubId() : null);
        dmir.setSubChargeCodeName(masInvest.getSubChargeCodeId() != null ? masInvest.getSubChargeCodeId().getSubName() : null);
        dmir.setSampleId(masInvest.getSampleId() != null ? masInvest.getSampleId().getId() : null);
        dmir.setSampleName(masInvest.getSampleId() != null ? masInvest.getSampleId().getSampleDescription() : null);
        dmir.setEquipmentId(masInvest.getEquipmentId());
        dmir.setCollectionId(masInvest.getCollectionId() != null ? masInvest.getCollectionId().getCollectionId() : null);
        dmir.setCollectionName(masInvest.getCollectionId() != null ? masInvest.getCollectionId().getCollectionName() : null);
        dmir.setBloodReactionTest(masInvest.getBloodReactionTest());
        dmir.setBloodBankScreenTest(masInvest.getBloodBankScreenTest());
        dmir.setInstructions(masInvest.getInstructions());
        dmir.setDiscountApplicable(masInvest.getDiscountApplicable());
        dmir.setGenderApplicable(masInvest.getGenderApplicable());
        dmir.setDiscount(masInvest.getDiscount());
        dmir.setPrice(masInvest.getPrice());

        // Sub Investigations
        List<DgSubMasInvestigationResponse> subInvestResponses = subInvestigationList.stream().map(sub -> {
            DgSubMasInvestigationResponse resp = new DgSubMasInvestigationResponse();
            resp.setSubInvestigationId(sub.getSubInvestigationId());
            resp.setSubInvestigationCode(sub.getSubInvestigationCode());
            resp.setSubInvestigationName(sub.getSubInvestigationName());
            resp.setStatus(sub.getStatus());
            resp.setOrderNo(sub.getOrderNo());
            resp.setLastChgBy(sub.getLastChgBy());
            resp.setLastChgDate(sub.getLastChgDate());
            resp.setLastChgTime(sub.getLastChgTime());
            resp.setResultType(sub.getResultType());
            resp.setComparisonType(sub.getComparisonType());
            resp.setMainChargeCodeId(sub.getMainChargeCodeId() != null ? sub.getMainChargeCodeId().getChargecodeId() : null);
            resp.setSubChargeCodeId(sub.getSubChargeCodeId() != null ? sub.getSubChargeCodeId().getSubId() : null);
            resp.setSampleId(sub.getSampleId() != null ? sub.getSampleId().getId() : null);
            resp.setUomId(sub.getUomId() != null ? sub.getUomId().getId() : null);
            resp.setInvestigationId(sub.getInvestigationId() != null ? sub.getInvestigationId().getInvestigationId() : null);
            return resp;
        }).collect(Collectors.toList());
        dmir.setSubInvestigationResponseList(subInvestResponses);

        // Fixed Values
        List<DgFixedValueResponse> fixedValResponses = fixedValList.stream().map(fixed -> {
            DgFixedValueResponse resp = new DgFixedValueResponse();
            resp.setFixedId(fixed.getFixedId());
            resp.setFixedValue(fixed.getFixedValue());
            resp.setSubChargeCodeId(fixed.getSubChargeCodeId() != null ? fixed.getSubChargeCodeId().getSubId() : 0L);
            return resp;
        }).collect(Collectors.toList());
        dmir.setFixedValueResponseList(fixedValResponses);

        // Normal Values
        List<DgNormalValueResponse> normalValResponses = normalValList.stream().map(normal -> {
            DgNormalValueResponse resp = new DgNormalValueResponse();
            resp.setNormalId(normal.getNormalId());
            resp.setSex(normal.getSex());
            resp.setFromAge(normal.getFromAge());
            resp.setToAge(normal.getToAge());
            resp.setMinNormalValue(normal.getMinNormalValue());
            resp.setMaxNormalValue(normal.getMaxNormalValue());
            resp.setNormalValue(normal.getNormalValue());
            resp.setSubInvestigationId(normal.getSubInvestigationId() != null ? normal.getSubInvestigationId().getSubInvestigationId() : null);
            resp.setMainChargeCodeId(normal.getMainChargeCodeId() != null ? normal.getMainChargeCodeId().getChargecodeId() : null);
            return resp;
        }).collect(Collectors.toList());
        dmir.setNormalValueResponseList(normalValResponses);

        return dmir;
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
