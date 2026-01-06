package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.DgMasInvestigationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private MasInvestigationCategoryRepository micRepo;

    @Autowired
    private MasInvestigationMethodologyRepository mimRepo;

    @Autowired
    private AuthUtil authUtil;

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
//        long startTime = System.currentTimeMillis();
        try {
            List<DgMasInvestigation> investigationList;
            if (flag == 1) {
                investigationList = dgMasInvestigationRepo.findByStatusIgnoreCaseOrderByLastChgDateDesc("Y");
            } else if (flag == 0) {
                investigationList = dgMasInvestigationRepo.findByStatusInIgnoreCaseOrderByLastChgDateDesc(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all, 1 for active.",
                        400
                );
            }

            if (investigationList == null || investigationList.isEmpty()) {
                return ResponseUtils.createSuccessResponse(Collections.emptyList(), new TypeReference<>() {});
            }

            List<Long> investigationIds = investigationList.stream()
                    .map(DgMasInvestigation::getInvestigationId)
                    .collect(Collectors.toList());

            List<DgSubMasInvestigation> allSubList =
                    subInvestigationRepo.findByInvestigationId_InvestigationIdIn(investigationIds);

            List<DgFixedValue> allFixedList =
                    fixedRepo.findBySubInvestigationId_InvestigationId_InvestigationIdIn(investigationIds);

            List<DgNormalValue> allNormalList =
                    normalRepo.findBySubInvestigationId_InvestigationId_InvestigationIdIn(investigationIds);


            Map<Long, List<DgSubMasInvestigation>> subMap = allSubList.stream()
                    .collect(Collectors.groupingBy(sub -> sub.getInvestigationId().getInvestigationId()));

            Map<Long, List<DgFixedValue>> fixedMap = allFixedList.stream()
                    .collect(Collectors.groupingBy(f -> f.getSubInvestigationId().getInvestigationId().getInvestigationId()));

            Map<Long, List<DgNormalValue>> normalMap = allNormalList.stream()
                    .collect(Collectors.groupingBy(n -> n.getSubInvestigationId().getInvestigationId().getInvestigationId()));


            List<DgMasInvestigationResponse> responseList = investigationList.stream()
                    .map(masInvest -> {
                        Long id = masInvest.getInvestigationId();
                        return mapToResponseMulti(
                                masInvest,
                                subMap.getOrDefault(id, Collections.emptyList()),
                                fixedMap.getOrDefault(id, Collections.emptyList()),
                                normalMap.getOrDefault(id, Collections.emptyList())
                        );
                    })
                    .collect(Collectors.toList());

//            long endTime = System.currentTimeMillis();
//            log.info("getAllInvestigations executed successfully in {} ms", endTime - startTime);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
//            long endTime = System.currentTimeMillis();
//            log.error("getAllInvestigations failed after {} ms. Error: {}", endTime - startTime, e.getMessage(), e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error retrieving investigations: " + e.getMessage(),
                    500
            );
        }
    }



    public ApiResponse<Page<DgMasInvestigationResponse>> getAllInvestigationsDynamic(
            int flag,
            int page,
            int size,
            String search,
            Long mainChargeCodeId) {

        try {
            Pageable pageable =
                    PageRequest.of(page, size, Sort.by("lastChgDate").descending());

            Page<DgMasInvestigation> investigationPage;

            /* -------------------------------------------------
               🔥 CORE DECISION LOGIC (VERY IMPORTANT)
               ------------------------------------------------- */
            if (search != null && !search.trim().isEmpty()) {

                investigationPage =
                        dgMasInvestigationRepo.searchInvestigations(
                                flag,
                                search.toLowerCase(),   // ❌ no %
                                mainChargeCodeId,
                                pageable
                        );

            } else if (mainChargeCodeId != null) {

                investigationPage =
                        dgMasInvestigationRepo.findAllWithFilter(
                                flag,
                                mainChargeCodeId,
                                pageable
                        );

            } else if (flag == 1) {

                investigationPage =
                        dgMasInvestigationRepo
                                .findByStatusIgnoreCase("Y", pageable);

            } else if (flag == 0) {

                investigationPage =
                        dgMasInvestigationRepo
                                .findByStatusInIgnoreCase(List.of("Y", "N"), pageable);

            } else {

                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1",
                        400
                );
            }

            /* -------------------------------------------------
               NO DATA FOUND
               ------------------------------------------------- */
            if (investigationPage.isEmpty()) {
                return ResponseUtils.createSuccessResponse(
                        Page.empty(pageable),
                        new TypeReference<>() {}
                );
            }

            /* -------------------------------------------------
               FETCH CHILD DATA (PAGE-SCOPED)
               ------------------------------------------------- */
            List<Long> investigationIds =
                    investigationPage.getContent()
                            .stream()
                            .map(DgMasInvestigation::getInvestigationId)
                            .toList();

            Map<Long, List<DgSubMasInvestigation>> subMap =
                    subInvestigationRepo
                            .findByInvestigationId_InvestigationIdIn(investigationIds)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    s -> s.getInvestigationId().getInvestigationId()
                            ));

            Map<Long, List<DgFixedValue>> fixedMap =
                    fixedRepo
                            .findBySubInvestigationId_InvestigationId_InvestigationIdIn(investigationIds)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    f -> f.getSubInvestigationId()
                                            .getInvestigationId()
                                            .getInvestigationId()
                            ));

            Map<Long, List<DgNormalValue>> normalMap =
                    normalRepo
                            .findBySubInvestigationId_InvestigationId_InvestigationIdIn(investigationIds)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    n -> n.getSubInvestigationId()
                                            .getInvestigationId()
                                            .getInvestigationId()
                            ));

            /* -------------------------------------------------
               MAP TO RESPONSE DTO
               ------------------------------------------------- */
            Page<DgMasInvestigationResponse> responsePage =
                    investigationPage.map(mas ->
                            mapToResponseMulti(
                                    mas,
                                    subMap.getOrDefault(
                                            mas.getInvestigationId(), List.of()
                                    ),
                                    fixedMap.getOrDefault(
                                            mas.getInvestigationId(), List.of()
                                    ),
                                    normalMap.getOrDefault(
                                            mas.getInvestigationId(), List.of()
                                    )
                            )
                    );

            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error retrieving investigations: " + e.getMessage(),
                    500
            );
        }
    }



    @Override
    public ApiResponse<String> changeInvestigationStatus(Long investigationId, String status) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            Optional<DgMasInvestigation> dgMasInvestigationOpt = dgMasInvestigationRepo.findById(investigationId);
            if(dgMasInvestigationOpt.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Invalid Investigation ID",HttpStatus.NOT_FOUND.value());
            }
            DgMasInvestigation dgMasInvestigation = dgMasInvestigationOpt.get();
            dgMasInvestigation.setStatus(status);
            dgMasInvestigation.setLastChgBy(currentUser.getUsername());
            dgMasInvestigation.setLastChgDate(Instant.now());
            dgMasInvestigation.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            dgMasInvestigationRepo.save(dgMasInvestigation);
            return  ResponseUtils.createSuccessResponse("Investigation Status Changed Successfully", new TypeReference<>() {}) ;
        } catch (Exception e) {
            log.error("Unexpected error changing investigation status: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<DgMasInvestigationSingleResponse> createInvestigation(DgMasInvestigationSingleReqest investigationRequest) {
        try{
            DgMasInvestigation masInvestigation = new DgMasInvestigation();
            masInvestigation.setInvestigationName(investigationRequest.getInvestigationName());
            masInvestigation.setStatus("y");
            masInvestigation.setConfidential(investigationRequest.getConfidential());
            masInvestigation.setInvestigationType(investigationRequest.getInvestigationType());
            if ("m".equalsIgnoreCase(investigationRequest.getInvestigationType())) {
                masInvestigation.setMultipleResults("y");
            } else {
                masInvestigation.setMultipleResults("n");
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            masInvestigation.setLastChgBy(currentUser.getUsername());
            masInvestigation.setLastChgDate(Instant.now());
            masInvestigation.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            masInvestigation.setMaxNormalValue(investigationRequest.getMaxNormalValue());
            masInvestigation.setMinNormalValue(investigationRequest.getMinNormalValue());
            Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(investigationRequest.getMainChargeCodeId());
            masInvestigation.setMainChargeCodeId(mmcc.get());
            if(investigationRequest.getUomId()!=null){
                Optional<DgUom> du = uomRepo.findById(investigationRequest.getUomId());
                masInvestigation.setUomId(du.orElseThrow());
            }else{
                masInvestigation.setUomId(null);
            }
            Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(investigationRequest.getSubChargeCodeId());
            masInvestigation.setSubChargeCodeId(mscc.get());
            Optional<DgMasSample> dms = sampleRepo.findById(investigationRequest.getSampleId());
            masInvestigation.setSampleId(dms.get());
            Optional<DgMasCollection> dmc = collectionRepo.findById(investigationRequest.getCollectionId());
            masInvestigation.setCollectionId(dmc.get());
            masInvestigation.setGenderApplicable(investigationRequest.getGenderApplicable());
            Optional<MasInvestigationCategory> mic = micRepo.findById(investigationRequest.getCategoryId());
            masInvestigation.setCategoryId(mic.get());
            Optional<MasInvestigationMethodology> mim = mimRepo.findById(investigationRequest.getMethodId());
            masInvestigation.setMethodId(mim.get());
            masInvestigation.setInterpretation(investigationRequest.getInterpretation());
            masInvestigation.setPreparationText(investigationRequest.getPreparationRequired());
            masInvestigation.setTatHours(investigationRequest.getTatHours());
            masInvestigation.setEstimatedDays(investigationRequest.getEstimatedDays());
//            masInvestigation.setMultipleResults(investigationRequest.getMultipleResults());
//            masInvestigation.setQuantity(investigationRequest.getQuantity());
//            masInvestigation.setNormalValue(investigationRequest.getNormalValue());
//            masInvestigation.setAppointmentRequired(investigationRequest.getAppointmentRequired());
//            masInvestigation.setTestOrderNo(investigationRequest.getTestOrderNo());
//            masInvestigation.setNumericOrString(investigationRequest.getNumericOrString());
//            masInvestigation.setHicCode(investigationRequest.getHicCode());
//            masInvestigation.setEquipmentId(investigationRequest.getEquipmentId());
//            masInvestigation.setAppearInDischargeSummary(investigationRequest.getAppearInDischargeSummary());
//            masInvestigation.setBloodReactionTest(investigationRequest.getBloodReactionTest());
//            masInvestigation.setBloodBankScreenTest(investigationRequest.getBloodBankScreenTest());
//            masInvestigation.setInstructions(investigationRequest.getInstructions());
//            masInvestigation.setDiscountApplicable(investigationRequest.getDiscountApplicable());
//            masInvestigation.setDiscount(investigationRequest.getDiscount());
//            masInvestigation.setPrice(investigationRequest.getPrice());
            return ResponseUtils.createSuccessResponse(mapToResponse(dgMasInvestigationRepo.save(masInvestigation)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Data was not appended properly" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<DgMasInvestigationSingleResponse> updateSingleInvestigation(Long investigationId, DgMasInvestigationSingleReqest investigationRequest) {
        User currentUser = authUtil.getCurrentUser();
        try{
            Optional<DgMasInvestigation> masInvestigation = dgMasInvestigationRepo.findById(investigationId);
            if (masInvestigation.isPresent()) {
                DgMasInvestigation dmi = masInvestigation.get();
                dmi.setInvestigationName(investigationRequest.getInvestigationName());
                dmi.setConfidential(investigationRequest.getConfidential());
                dmi.setInvestigationType(investigationRequest.getInvestigationType());
                dmi.setLastChgBy(currentUser.getUsername());
                dmi.setLastChgDate(Instant.now());
                dmi.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                dmi.setMaxNormalValue(investigationRequest.getMaxNormalValue());
                dmi.setMinNormalValue(investigationRequest.getMinNormalValue());
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
                }else{
                    dmi.setUomId(null);
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
                if (investigationRequest.getCollectionId() != null) {
                    Optional<DgMasCollection> dmc = collectionRepo.findById(investigationRequest.getCollectionId());
                    if (dmc.isPresent()) {
                        dmi.setCollectionId(dmc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("collectionId not found", 404);
                    }
                }
                dmi.setGenderApplicable(investigationRequest.getGenderApplicable());

                if (investigationRequest.getCategoryId() != null) {
                    Optional<MasInvestigationCategory> mic = micRepo.findById(investigationRequest.getCategoryId());
                    if (mic.isPresent()) {
                        dmi.setCategoryId(mic.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("categoryId not found", 404);
                    }
                }
                if (investigationRequest.getMethodId() != null) {
                    Optional<MasInvestigationMethodology> mim = mimRepo.findById(investigationRequest.getMethodId());
                    if(mim.isPresent()) {
                        dmi.setMethodId(mim.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("methodId not found", 404);
                    }
                }
                dmi.setInterpretation(investigationRequest.getInterpretation());
                dmi.setEstimatedDays(investigationRequest.getEstimatedDays());
                dmi.setPreparationText(investigationRequest.getPreparationRequired());
                dmi.setTatHours(investigationRequest.getTatHours());
//                dmi.setAppearInDischargeSummary(investigationRequest.getAppearInDischargeSummary());
//                dmi.setMultipleResults(investigationRequest.getMultipleResults());
//                dmi.setQuantity(investigationRequest.getQuantity());
//                dmi.setNormalValue(investigationRequest.getNormalValue());
//                dmi.setAppointmentRequired(investigationRequest.getAppointmentRequired());
//                dmi.setTestOrderNo(investigationRequest.getTestOrderNo());
//                dmi.setNumericOrString(investigationRequest.getNumericOrString());
//                dmi.setHicCode(investigationRequest.getHicCode());
//                dmi.setEquipmentId(investigationRequest.getEquipmentId());
//                dmi.setBloodReactionTest(investigationRequest.getBloodReactionTest());
//                dmi.setBloodBankScreenTest(investigationRequest.getBloodBankScreenTest());
//                dmi.setInstructions(investigationRequest.getInstructions());
//                dmi.setDiscountApplicable(investigationRequest.getDiscountApplicable());
//                dmi.setDiscount(investigationRequest.getDiscount());
//                dmi.setPrice(investigationRequest.getPrice());
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
    @Transactional
    public ApiResponse<String> updateMultipleInvestigation(DgMasInvestigationMultiRequest multiRequest) {
        User currentUser = authUtil.getCurrentUser();
        Optional<DgMasInvestigation> masInvestOpt = dgMasInvestigationRepo.findById(multiRequest.getInvestigationId());
        DgMasInvestigation masInvestigation = masInvestOpt.get();
        masInvestigation.setInvestigationName(multiRequest.getInvestigationName());
        masInvestigation.setConfidential(multiRequest.getConfidential());
        masInvestigation.setInvestigationType(multiRequest.getInvestigationType());
        masInvestigation.setLastChgBy(currentUser.getUsername());
        masInvestigation.setLastChgDate(Instant.now());
        masInvestigation.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        masInvestigation.setMaxNormalValue(multiRequest.getMaxNormalValue());
        masInvestigation.setMinNormalValue(multiRequest.getMinNormalValue());
        masInvestigation.setInterpretation(multiRequest.getInterpretation());
        if (multiRequest.getMainChargeCodeId() != null) {
            Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(multiRequest.getMainChargeCodeId());
            if (mmcc.isPresent()) {
                masInvestigation.setMainChargeCodeId(mmcc.get());
            } else {
                return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
            }
        }
        if (multiRequest.getUomId() != null) {
            Optional<DgUom> du = uomRepo.findById(multiRequest.getUomId());
            if (du.isPresent()) {
                masInvestigation.setUomId(du.get());
            } else {
                return ResponseUtils.createNotFoundResponse("uomId not found", 404);
            }
        }else{
            masInvestigation.setUomId(null);
        }
        if (multiRequest.getSubChargeCodeId() != null) {
            Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(multiRequest.getSubChargeCodeId());
            if (mscc.isPresent()) {
                masInvestigation.setSubChargeCodeId(mscc.get());
            } else {
                return ResponseUtils.createNotFoundResponse("subChargeCodeId not found", 404);
            }
        }
        if (multiRequest.getSampleId() != null) {
            Optional<DgMasSample> dms = sampleRepo.findById(multiRequest.getSampleId());
            if (dms.isPresent()) {
                masInvestigation.setSampleId(dms.get());
            } else {
                return ResponseUtils.createNotFoundResponse("sampleId not found", 404);
            }
        }
        if (multiRequest.getCollectionId() != null) {
            Optional<DgMasCollection> dmc = collectionRepo.findById(multiRequest.getCollectionId());
            if (dmc.isPresent()) {
                masInvestigation.setCollectionId(dmc.get());
            } else {
                return ResponseUtils.createNotFoundResponse("collectionId not found", 404);
            }
        }
        masInvestigation.setGenderApplicable(multiRequest.getGenderApplicable());
        if (multiRequest.getCategoryId() != null) {
            Optional<MasInvestigationCategory> mic = micRepo.findById(multiRequest.getCategoryId());
            if(mic.isPresent()) {
                masInvestigation.setCategoryId(mic.get());
            } else {
                return ResponseUtils.createNotFoundResponse("categoryId not found", 404);
            }
        }
        if(multiRequest.getMethodId() != null) {
            Optional<MasInvestigationMethodology> mim = mimRepo.findById(multiRequest.getMethodId());
            if(mim.isPresent()) {
                masInvestigation.setMethodId(mim.get());
            } else {
                return ResponseUtils.createNotFoundResponse("methodId not found", 404);
            }
        }
        masInvestigation.setTatHours(multiRequest.getTatHours());
        masInvestigation.setPreparationText(multiRequest.getPreparationRequired());
        masInvestigation.setEstimatedDays(multiRequest.getEstimatedDays());
        dgMasInvestigationRepo.save(masInvestigation);

        if(masInvestigation != null){
            for (DgSubMasInvestigationRequest subInvestObj : multiRequest.getMasInvestReq()) {
                if (subInvestObj.getSubInvestigationId() == null || subInvestObj.getSubInvestigationId() == 0) {
//                    adding sub_mas_investigation here
                    DgSubMasInvestigation newSubObj = new DgSubMasInvestigation();
                    newSubObj.setSubInvestigationCode(subInvestObj.getSubInvestigationCode());
                    newSubObj.setSubInvestigationName(subInvestObj.getSubInvestigationName());
                    newSubObj.setStatus("y");
                    newSubObj.setResultType(subInvestObj.getResultType());
                    newSubObj.setComparisonType(subInvestObj.getComparisonType());
                    newSubObj.setLastChgBy(currentUser.getUsername());
                    newSubObj.setLastChgDate(Instant.now());
                    newSubObj.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(subInvestObj.getMainChargeCodeId());
                    if(mmcc.isPresent()) {
                        newSubObj.setMainChargeCodeId(mmcc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
                    }
                    newSubObj.setSampleId(masInvestigation.getSampleId());
                    Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(subInvestObj.getSubChargeCodeId());
                    if(mscc.isPresent()) {
                        newSubObj.setSubChargeCodeId(mscc.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("subChargeCodeId not found", 404);
                    }
                    Optional<DgUom> du = uomRepo.findById(subInvestObj.getUomId());
                    if(du.isPresent()) {
                        newSubObj.setUomId(du.get());
                    } else {
                        return ResponseUtils.createNotFoundResponse("uomId not found", 404);
                    }
                    newSubObj.setInvestigationId(masInvestigation);
                    newSubObj.setFixedValueExpectedValue(subInvestObj.getFixedValueExpectedResult());
                    subInvestigationRepo.save(newSubObj);

//                    condition for comparison type
                    if ("n".equalsIgnoreCase(subInvestObj.getComparisonType())) {
                        // delete normal values by normalId
                        if (subInvestObj.getNormalValueIdsToDelete() != null) {
                            for (Long normalId : subInvestObj.getNormalValueIdsToDelete()) {
                                if (normalId != null && normalId != 0) {
                                    normalRepo.deleteById(normalId);
                                }
                            }
                        }

                        // add or update normal values
                        if (subInvestObj.getNormalValues() != null) {
                            for (DgNormalValueRequest nv : subInvestObj.getNormalValues()) {
                                DgNormalValue entity;
                                if (nv.getNormalId() != null && nv.getNormalId() != 0) {
                                    entity = normalRepo.findById(nv.getNormalId()).orElse(new DgNormalValue());
                                } else {
                                    entity = new DgNormalValue();
                                }
                                entity.setSex(nv.getSex());
                                entity.setFromAge(nv.getFromAge());
                                entity.setToAge(nv.getToAge());
                                entity.setMinNormalValue(nv.getMinNormalValue());
                                entity.setMaxNormalValue(nv.getMaxNormalValue());
                                entity.setNormalValue(nv.getNormalValue());
                                Optional<MasMainChargeCode> mmccObj = mainChargeCodeRepo.findById(nv.getMainChargeCodeId());
                                if (mmccObj.isPresent()) {
                                    entity.setMainChargeCodeId(mmccObj.get());
                                } else {
                                    return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
                                }
                                entity.setSubInvestigationId(newSubObj);
                                normalRepo.save(entity);
                            }
                        }

                    } else if ("f".equalsIgnoreCase(subInvestObj.getComparisonType())) {
                        // deleting fixed values by fixedId
                        if (subInvestObj.getFixedValueIdsToDelete() != null) {
                            for (Long fixedId : subInvestObj.getFixedValueIdsToDelete()) {
                                if (fixedId != null && fixedId != 0) {
                                    fixedRepo.deleteById(fixedId);
                                }
                            }
                        }

                        // add/update fixed values
                        if (subInvestObj.getFixedValues() != null) {
                            for (DgFixedValueRequest fv : subInvestObj.getFixedValues()) {
                                DgFixedValue entity;
                                if (fv.getFixedId() != null && fv.getFixedId() != 0) {
                                    entity = fixedRepo.findById(fv.getFixedId()).orElse(new DgFixedValue());
                                } else {
                                    entity = new DgFixedValue();
                                }
                                entity.setFixedValue(fv.getFixedValue());
                                entity.setSubInvestigationId(newSubObj);
                                fixedRepo.save(entity);
                            }
                        }
                    }

                } else {

//                    updating sub_mas_investigation here
                    Optional<DgSubMasInvestigation> existingOpt = subInvestigationRepo.findById(subInvestObj.getSubInvestigationId());
                    if (existingOpt.isPresent()) {
                        DgSubMasInvestigation existing = existingOpt.get();
                        existing.setSubInvestigationName(subInvestObj.getSubInvestigationName());
                        existing.setSubInvestigationCode(subInvestObj.getSubInvestigationCode());
                        existing.setResultType(subInvestObj.getResultType());
                        existing.setComparisonType(subInvestObj.getComparisonType());
                        existing.setLastChgBy(currentUser.getUsername());
                        existing.setLastChgDate(Instant.now());
                        existing.setLastChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        Optional<MasMainChargeCode> mmcc = mainChargeCodeRepo.findById(subInvestObj.getMainChargeCodeId());
                        existing.setMainChargeCodeId(mmcc.get());
                        Optional<MasSubChargeCode> mscc = subChargeCodeRepo.findById(subInvestObj.getSubChargeCodeId());
                        existing.setSubChargeCodeId(mscc.get());
                        existing.setSampleId(masInvestigation.getSampleId());
                        Optional<DgUom> du = uomRepo.findById(subInvestObj.getUomId());
                        existing.setUomId(du.get());
                        existing.setInvestigationId(masInvestigation); // Ensure linkage
                        existing.setFixedValueExpectedValue(subInvestObj.getFixedValueExpectedResult());
                        subInvestigationRepo.save(existing);

                        if ("n".equalsIgnoreCase(subInvestObj.getComparisonType())) {
                            // delete normal values by normalId
                            if (subInvestObj.getNormalValueIdsToDelete() != null) {
                                for (Long normalId : subInvestObj.getNormalValueIdsToDelete()) {
                                    if (normalId != null && normalId != 0) {
                                        normalRepo.deleteById(normalId);
                                    }
                                }
                            }

                            // add or update normal values
                            if (subInvestObj.getNormalValues() != null) {
                                for (DgNormalValueRequest nv : subInvestObj.getNormalValues()) {
                                    DgNormalValue entity;
                                    if (nv.getNormalId() != null && nv.getNormalId() != 0) {
                                        entity = normalRepo.findById(nv.getNormalId()).orElse(new DgNormalValue());
                                    } else {
                                        entity = new DgNormalValue();
                                    }
                                    entity.setSex(nv.getSex());
                                    entity.setFromAge(nv.getFromAge());
                                    entity.setToAge(nv.getToAge());
                                    entity.setMinNormalValue(nv.getMinNormalValue());
                                    entity.setMaxNormalValue(nv.getMaxNormalValue());
                                    entity.setNormalValue(nv.getNormalValue());
                                    Optional<MasMainChargeCode> mmccObj = mainChargeCodeRepo.findById(nv.getMainChargeCodeId());
                                    if (mmccObj.isPresent()) {
                                        entity.setMainChargeCodeId(mmccObj.get());
                                    } else {
                                        return ResponseUtils.createNotFoundResponse("masMainChargeCodeId not found", 404);
                                    }
                                    entity.setSubInvestigationId(existing);
                                    normalRepo.save(entity);
                                }
                            }

                        } else if ("f".equalsIgnoreCase(subInvestObj.getComparisonType())) {
                            // deleting fixed values by fixedId
                            if (subInvestObj.getFixedValueIdsToDelete() != null) {
                                for (Long fixedId : subInvestObj.getFixedValueIdsToDelete()) {
                                    if (fixedId != null && fixedId != 0) {
                                        fixedRepo.deleteById(fixedId);
                                    }
                                }
                            }

                            // add/update fixed values
                            if (subInvestObj.getFixedValues() != null) {
                                for (DgFixedValueRequest fv : subInvestObj.getFixedValues()) {
                                    DgFixedValue entity;
                                    if (fv.getFixedId() != null && fv.getFixedId() != 0) {
                                        entity = fixedRepo.findById(fv.getFixedId()).orElse(new DgFixedValue());
                                    } else {
                                        entity = new DgFixedValue();
                                    }
                                    entity.setFixedValue(fv.getFixedValue());
                                    entity.setSubInvestigationId(existing);
                                    fixedRepo.save(entity);
                                }
                            }
                        }
                    }
                }
            }
            if (multiRequest.getSubInvestigationIdsToDelete() != null
                    && !multiRequest.getSubInvestigationIdsToDelete().isEmpty()) {
                for (Long subIdToDelete : multiRequest.getSubInvestigationIdsToDelete()) {
                    if (subIdToDelete != null && subIdToDelete != 0){
                        Optional<DgSubMasInvestigation> subOpt = subInvestigationRepo.findById(subIdToDelete);
                        if (subOpt.isPresent()) {
                            DgSubMasInvestigation sub = subOpt.get();
                            sub.setStatus("n");
                            subInvestigationRepo.save(sub);
                        }
                    }
                }
            }
        } else {
            return ResponseUtils.createSuccessResponse("Investigation ID not found", new TypeReference<>() {
            });
        }
        return ResponseUtils.createSuccessResponse("update Successfully", new TypeReference<>() {
        });

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
        dmir.setLastChgBy(masInvest.getLastChgBy());
        dmir.setLastChgDate(masInvest.getLastChgDate());
        dmir.setLastChgTime(masInvest.getLastChgTime());
        dmir.setConfidential(masInvest.getConfidential());
        dmir.setInvestigationType(masInvest.getInvestigationType());
        dmir.setMultipleResults(masInvest.getMultipleResults());
        dmir.setMaxNormalValue(masInvest.getMaxNormalValue());
        dmir.setMinNormalValue(masInvest.getMinNormalValue());
        dmir.setMainChargeCodeId(masInvest.getMainChargeCodeId() != null ? masInvest.getMainChargeCodeId().getChargecodeId() : null);
        dmir.setMainChargeCodeName(masInvest.getMainChargeCodeId() != null ? masInvest.getMainChargeCodeId().getChargecodeName() : null);
        dmir.setUomId(masInvest.getUomId() != null ? masInvest.getUomId().getId() : null);
        dmir.setUomName(masInvest.getUomId() != null ? masInvest.getUomId().getName() : null);
        dmir.setCategoryId(masInvest.getCategoryId() != null ? masInvest.getCategoryId().getCategoryId() : null);
        dmir.setCategoryName(masInvest.getCategoryId() != null ? masInvest.getCategoryId().getCategoryName() :  null);
        dmir.setMethodId(masInvest.getMethodId() != null ? masInvest.getMethodId().getMethodId() : null);
        dmir.setMethodName(masInvest.getMethodId() != null ? masInvest.getMethodId().getMethodName() : null);
        dmir.setSubChargeCodeId(masInvest.getSubChargeCodeId() != null ? masInvest.getSubChargeCodeId().getSubId() : null);
        dmir.setSubChargeCodeName(masInvest.getSubChargeCodeId() != null ? masInvest.getSubChargeCodeId().getSubName() : null);
        dmir.setSampleId(masInvest.getSampleId() != null ? masInvest.getSampleId().getId() : null);
        dmir.setSampleName(masInvest.getSampleId() != null ? masInvest.getSampleId().getSampleDescription() : null);
        dmir.setCollectionId(masInvest.getCollectionId() != null ? masInvest.getCollectionId().getCollectionId() : null);
        dmir.setCollectionName(masInvest.getCollectionId() != null ? masInvest.getCollectionId().getCollectionName() : null);
        dmir.setCollectionName(masInvest.getCollectionId() != null ? masInvest.getCollectionId().getCollectionName() : null);
        dmir.setInterpretation(masInvest.getInterpretation() != null ? masInvest.getInterpretation(): null);
        dmir.setGenderApplicable(masInvest.getGenderApplicable());
        dmir.setPreparationText(masInvest.getPreparationText());
        dmir.setTatHours(masInvest.getTatHours());
        dmir.setEstimatedDays(masInvest.getEstimatedDays());

        // --- Group fixed values by subInvestigationId ---
        Map<Long, List<DgFixedValueResponse>> fixedBySub = fixedValList.stream()
                .map(fixed -> {
                    DgFixedValueResponse resp = new DgFixedValueResponse();
                    resp.setFixedId(fixed.getFixedId());
//                    resp.setLas
                    resp.setFixedValue(fixed.getFixedValue());
                    resp.setSubInvestigationId(
                            fixed.getSubInvestigationId() != null ? fixed.getSubInvestigationId().getSubInvestigationId() : 0L
                    );
                    return resp;
                })
                .collect(Collectors.groupingBy(DgFixedValueResponse::getSubInvestigationId));

        // --- Group normal values by subInvestigationId ---
        Map<Long, List<DgNormalValueResponse>> normalBySub = normalValList.stream()
                .map(normal -> {
                    DgNormalValueResponse resp = new DgNormalValueResponse();
                    resp.setNormalId(normal.getNormalId());
                    resp.setSex(normal.getSex());
                    resp.setFromAge(normal.getFromAge());
                    resp.setToAge(normal.getToAge());
                    resp.setMinNormalValue(normal.getMinNormalValue());
                    resp.setMaxNormalValue(normal.getMaxNormalValue());
                    resp.setNormalValue(normal.getNormalValue());
                    resp.setSubInvestigationId(
                            normal.getSubInvestigationId() != null ? normal.getSubInvestigationId().getSubInvestigationId() : null
                    );
                    resp.setMainChargeCodeId(
                            normal.getMainChargeCodeId() != null ? normal.getMainChargeCodeId().getChargecodeId() : null
                    );
                    return resp;
                })
                .collect(Collectors.groupingBy(DgNormalValueResponse::getSubInvestigationId));

        // --- Build SubInvestigation responses with nested fixed/normal values ---
        List<DgSubMasInvestigationResponse> subInvestResponses = subInvestigationList.stream().map(sub -> {
            DgSubMasInvestigationResponse resp = new DgSubMasInvestigationResponse();
            resp.setSubInvestigationId(sub.getSubInvestigationId());
            resp.setSubInvestigationCode(sub.getSubInvestigationCode());
            resp.setSubInvestigationName(sub.getSubInvestigationName());
            resp.setStatus(sub.getStatus());
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
            resp.setFixedValueExpectedResult(sub.getFixedValueExpectedValue());

            // attach fixed + normal values for this subInvestigation
            resp.setFixedValueResponseList(fixedBySub.getOrDefault(sub.getSubInvestigationId(), Collections.emptyList()));
            resp.setNormalValueResponseList(normalBySub.getOrDefault(sub.getSubInvestigationId(), Collections.emptyList()));

            return resp;
        }).collect(Collectors.toList());

        dmir.setSubInvestigationResponseList(subInvestResponses);

        return dmir;
    }

    private DgMasInvestigationSingleResponse mapToResponse(DgMasInvestigation entity) {
        DgMasInvestigationSingleResponse dto = new DgMasInvestigationSingleResponse();
        dto.setInvestigationId(entity.getInvestigationId());
        dto.setInvestigationName(entity.getInvestigationName());
        dto.setStatus(entity.getStatus());
        dto.setLastChgBy(entity.getLastChgBy());
        dto.setLastChgTime(entity.getLastChgTime());
        dto.setLastChgDate(entity.getLastChgDate());
        dto.setConfidential(entity.getConfidential());
        dto.setInvestigationType(entity.getInvestigationType());
        dto.setMultipleResults(entity.getMultipleResults());
        dto.setMaxNormalValue(entity.getMaxNormalValue());
        dto.setMinNormalValue(entity.getMinNormalValue());
        dto.setMainChargeCodeId(entity.getMainChargeCodeId() != null ? entity.getMainChargeCodeId().getChargecodeId() : null);
        dto.setMainChargeCodeName(entity.getMainChargeCodeId() != null ? entity.getMainChargeCodeId().getChargecodeName() : null);
        dto.setUomId(entity.getUomId() != null ? entity.getUomId().getId() : null);
        dto.setUomName(entity.getUomId() != null ? entity.getUomId().getName() : null);
        dto.setCategoryId(entity.getCategoryId() != null ? entity.getCategoryId().getCategoryId() : null);
        dto.setCategoryName(entity.getCategoryId() != null ? entity.getCategoryId().getCategoryName() : null);
        dto.setMethodId(entity.getMethodId() != null ? entity.getMethodId().getMethodId() : null);
        dto.setMethodName(entity.getMethodId() != null ? entity.getMethodId().getMethodName() : null);
        dto.setSubChargeCodeId(entity.getSubChargeCodeId() != null ? entity.getSubChargeCodeId().getSubId() : null);
        dto.setSubChargeCodeName(entity.getSubChargeCodeId() != null ? entity.getSubChargeCodeId().getSubName() : null);
        dto.setSampleId(entity.getSampleId() != null ? entity.getSampleId().getId() : null);
        dto.setSampleName(entity.getSampleId() != null ? entity.getSampleId().getSampleDescription() : null);
        dto.setCollectionId(entity.getCollectionId() != null ? entity.getCollectionId().getCollectionId() : null);
        dto.setCollectionName(entity.getCollectionId() != null ? entity.getCollectionId().getCollectionName() : null);
        dto.setGenderApplicable(entity.getGenderApplicable());
        dto.setInterpretation(entity.getInterpretation());
//        dto.setQuantity(entity.getQuantity());
//        dto.setNormalValue(entity.getNormalValue());
//        dto.setAppointmentRequired(entity.getAppointmentRequired());
//        dto.setTestOrderNo(entity.getTestOrderNo());
//        dto.setNumericOrString(entity.getNumericOrString());
//        dto.setHicCode(entity.getHicCode());
//        dto.setEquipmentId(entity.getEquipmentId());
//        dto.setAppearInDischargeSummary(entity.getAppearInDischargeSummary());
//        dto.setBloodReactionTest(entity.getBloodReactionTest());
//        dto.setBloodBankScreenTest(entity.getBloodBankScreenTest());
//        dto.setInstructions(entity.getInstructions());
//        dto.setDiscountApplicable(entity.getDiscountApplicable());
//        dto.setDiscount(entity.getDiscount());
//        dto.setPrice(entity.getPrice());
        return dto;
    }



    @Override
    public List<Map<String, Object>> getInvestigationTypes() {

        return dgMasInvestigationRepo.findUniqueInvestigationTypes()
                .stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put(
                            "value",
                            p.getName().toLowerCase().replace(" ", "-")
                    );
                    return map;
                })
                .toList();
    }

    @Override
    public ApiResponse<List<DgMasInvestigationRes>> getAllInvestigations() {
       try {
           List<DgMasInvestigation> investigations=dgMasInvestigationRepo.findByStatusIgnoreCaseOrderByLastChgDateDesc("y");
           return ResponseUtils.createSuccessResponse(investigations.stream().map(this::mapToRes).toList(), new TypeReference<>() {});
       } catch (Exception e) {
           log.error("getAllInvestigations() Unexpected error::",e);
           return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }
    private DgMasInvestigationRes mapToRes(DgMasInvestigation entity){
        DgMasInvestigationRes response= new DgMasInvestigationRes();
        response.setInvestigationId(entity.getInvestigationId());
        response.setInvestigationName(entity.getInvestigationName());
        return  response;
    }
}
