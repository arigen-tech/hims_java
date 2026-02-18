package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.ResultService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class ResultServiceImpl implements ResultService {
    @Autowired
    private DgResultEntryHeaderRepository headerRepo;
    @Autowired
    private DgResultEntryDetailRepository detailRepo;
    @Autowired
    private DgSampleCollectionHeaderRepository sampleCollectionHeaderRepo;
    @Autowired
    private MasSubChargeCodeRepository subChargeRepo;
    @Autowired
    private DgSampleCollectionHeaderRepository dgSampleCollectionHeaderRepository;
    @Autowired
    MasMainChargeCodeRepository mainChargeCodeRepository;
    @Autowired
    MasRelationRepository masRelationRepository;
    @Autowired
    DgMasInvestigationRepository dgMasInvestigationRepository;
    @Autowired
    MasDepartmentRepository masDepartmentRepository;
    @Autowired
    DgMasSampleRepository masSampleRepository;
    @Autowired
    DgSubMasInvestigationRepository dgSubMasInvestigationRepository;
    @Autowired
    private DgSampleCollectionDetailsRepository dgSampleCollectionDetailsRepository;
    @Autowired
    private DgNormalValueRepository dgNormalValueRepository;
    @Autowired
    private DgFixedValueRepository dgFixedValueRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private LabHdRepository labHdRepository;
    @Autowired
    private LabTurnAroundTimeRepository labTurnAroundTimeRepository;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private LabDtRepository dgOrderDtRepository;
    @Autowired
    private LabHdRepository dgOrderHDRepository;


    @Autowired
    private MasLabResultAmendmentTypeRepository masLabResultAmendmentTypeRepository;

    @Autowired
    private LabResultAmendAuditRepository labResultAmendAuditRepository;


    @Autowired
    AuthUtil authUtil;

    @Autowired
    private LabOrderTrackingStatusRepository orderTrackingStatusRepository;

    @Value("${lab.track-order-status-result.entry}")
    private Long resulEntryStatusId;

    @Value("${lab.track-order-status-result.validate}")
    private Long resultValidatedStatusId;

    private final RandomNumGenerator randomNumGenerator;

    public  ResultServiceImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;

    }

    private String getCurrentTimeFormatted(Instant instant) {
        LocalTime time = instant
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("RES",true,true);
    }


    @Override
    @Transactional
    public ApiResponse<String>  saveOrUpdateResultEntry(ResultEntryMainRequest request) {
        log.info("Starting saveOrUpdateResultEntry for patientId={}, visitId={}",
                request.getPatientId(), request.getVisitId());
        try {
            Long depart = authUtil.getCurrentDepartmentId();
            MasDepartment depObj = masDepartmentRepository.getById(depart);
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            log.debug("Current user={}, department={}", currentUser.getUsername(), depObj.getDepartmentName());
            //  Check if header already exists for same Sample + SubChargeCode
            Optional<DgResultEntryHeader> existingHeaderOpt =
                    headerRepo.findBySampleCollectionHeaderId_SampleCollectionHeaderIdAndSubChargeCodeId_SubId(
                            request.getSampleCollectionHeaderId(),
                            request.getSubChargeCodeId()
                    );

            DgResultEntryHeader header;
            DgOrderHd dgOrderH=labHdRepository.findByPatientId_IdAndVisitId_Id(request.getPatientId(),request.getVisitId());
            Patient patientId = patientRepository.findById(request.getPatientId()).orElse(null);
            if (existingHeaderOpt.isPresent()) {
                log.info("Updating existing ResultEntryHeader id={}", existingHeaderOpt.get().getResultEntryId());

                // Update existing header
                header = existingHeaderOpt.get();
                header.setRemarks(request.getClinicalNotes());
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setResultStatus("n");
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
            } else {
                log.info("Creating new ResultEntryHeader for sampleCollectionHeaderId={}",
                        request.getSampleCollectionHeaderId());

                // Create new header
                header = new DgResultEntryHeader();
                header.setRelationId(masRelationRepository.findById(request.getRelationId()).orElse(null));
                header.setRemarks(request.getClinicalNotes());
                header.setResultDate(LocalDate.now());

                header.setResultTime(String.valueOf(LocalTime.now()));

                DgSampleCollectionHeader dgSampleCollectionHeader =
                        dgSampleCollectionHeaderRepository.findById(request.getSampleCollectionHeaderId())
                                .orElseThrow(() -> new RuntimeException("Invalid Sample Header ID: " + request.getSampleCollectionHeaderId()));

                header.setSampleCollectionHeaderId(dgSampleCollectionHeader);
                header.setResultStatus("n");
                //  header.setVerified("n");
                header.setDepartmentId(depObj);
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
                header.setResultNo(createInvoice());
                header.setHospitalId(currentUser.getHospital());
                header.setResultEnteredBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
//                Optional<DgOrderHd> dgOrderH=labHdRepository.findById(Math.toIntExact(request.getPatientId()));
//                header.setOrderHd(dgOrderH.get());
                // Optional<DgOrderHd> dgOrderH = labHdRepository.findByPatientId_IdAndOrderstatusN(request.getPatientId(),"n");

//                DgOrderHd orderHd = dgOrderH.orElseThrow(() ->
//                        new RuntimeException("No order found for patient ID: " + request.getPatientId()));
                header.setOrderHd(dgOrderH);
                header.setHinId(patientId);
                header.setMainChargecodeId(mainChargeCodeRepository.findById(request.getMainChargeCodeId()).orElse(null));
                header.setSubChargeCodeId(subChargeRepo.findById(request.getSubChargeCodeId()).orElse(null));
                header = headerRepo.save(header);
                log.info("ResultEntryHeader created with id={}", header.getResultEntryId());
            }
            // Save or Update Details
            for (ResultEntryInvestigationRequest invReq : request.getInvestigationList()) {
                log.debug("Processing investigationId={}", invReq.getInvestigationId());
                DgMasInvestigation investigation = dgMasInvestigationRepository.findById(invReq.getInvestigationId())
                        .orElseThrow(() -> new RuntimeException("Invalid Investigation ID: " + invReq.getInvestigationId()));
                DgSampleCollectionDetails dgSampleCollectionDetails =
                        dgSampleCollectionDetailsRepository.findById(invReq.getSampleCollectionDetailsId())
                                .orElseThrow(() -> new RuntimeException("Invalid sampleCollectionDetails ID: " + invReq.getSampleCollectionDetailsId()));

                // Check if ANY sub-investigation has a result
                boolean anyResultEntered = invReq.getResultEntryDetailsRequestList().stream()
                        .anyMatch(subReq -> subReq.getResult() != null && !subReq.getResult().trim().isEmpty());

                // If no sub-investigation result entered, skip this investigation entirely
                if (!anyResultEntered) {
                    log.debug("Skipping investigationId={} as no results entered",
                            invReq.getInvestigationId());
                    continue;
                }

                //  If at least one sub-investigation has result → process ALL sub-investigations
                for (ResultEntrySubInvestigationRequest subReq : invReq.getResultEntryDetailsRequestList()) {

                    //
                    DgSubMasInvestigation subInvestigation = null;
                    if (subReq.getSubInvestigationId() != null) {
                        subInvestigation = dgSubMasInvestigationRepository.findById(subReq.getSubInvestigationId())
                                .orElse(null);
                    }
                    Optional<DgResultEntryDetail> existingDetailOpt =
                            detailRepo.findByResultEntryIdAndInvestigationIdAndSubInvestigationId(
                                    header, investigation, subInvestigation
                            );

                    DgResultEntryDetail detail;
                    if (existingDetailOpt.isPresent()) {
                        // Update existing
                        detail = existingDetailOpt.get();
                        detail.setResult(subReq.getResult());
                        detail.setRemarks(subReq.getRemarks());
                    } else {
                        // Create new
                        detail = new DgResultEntryDetail();
                        detail.setResultEntryId(header);
                        detail.setInvestigationId(investigation);
                        detail.setSubInvestigationId(subInvestigation);
                        detail.setSampleCollectionDetailsId(dgSampleCollectionDetails);
                        detail.setResult(subReq.getResult());
                        detail.setRemarks(subReq.getRemarks());
                        detail.setResultType(subReq.getResultType());
                        detail.setValidated("n");
                        detail.setSampleId(masSampleRepository.findById(subReq.getSampleId()).orElse(null));
                        detail.setChargeCodeId(mainChargeCodeRepository.findById(request.getMainChargeCodeId()).orElse(null));
                        if("n".equalsIgnoreCase(subReq.getComparisonType())){
                            detail.setNormalId(dgNormalValueRepository.findById(subReq.getNormalId()).orElse(null));
                        }else if("f".equalsIgnoreCase(subReq.getComparisonType())){
                            detail.setFixedId(dgFixedValueRepository.findById(subReq.getFixedId()).orElse(null));
                        }else{
                            detail.setFixedId(null);
                            detail.setNormalId(null);
                        }
                        detail.setNormalRange(subReq.getNormalRange());
                        detail.setFixedValue(subReq.getFixedValue());
                        if(subInvestigation!=null){
                            detail.setUomId(subInvestigation.getUomId());
                        }else{
                            detail.setUomId(investigation.getUomId());

                        }
                        detail.setResultDetailStatus("n");
                        detail.setGeneratedSampleId(dgSampleCollectionDetails.getSampleGeneratedId());
                        log.debug("Created new ResultEntryDetail for investigationId={}",
                                investigation.getInvestigationId());
                    }

                    DgOrderDt dgOrderDt = dgOrderDtRepository.findByOrderhdId_IdAndInvestigationId_InvestigationId(dgOrderH.getId(), investigation.getInvestigationId());
                    DgOrderDt byId = dgOrderDtRepository.findById(dgOrderDt.getId()).orElseThrow(() -> new RuntimeException("Invalid Dg Order Dt Id"));
                    byId.setOrderTrackingStatus(orderTrackingStatusRepository.findById(resulEntryStatusId).orElseThrow());
                    dgOrderDtRepository.save(byId);


                    LabTurnAroundTime labTurnAroundTime=labTurnAroundTimeRepository.findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(dgOrderH.getId(),investigation.getInvestigationId(),patientId.getId(),false);
                    labTurnAroundTime.setResultEnteredBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                    labTurnAroundTime.setResultEntryDateTime(LocalDateTime.now());
                    labTurnAroundTimeRepository.save( labTurnAroundTime);
                    detailRepo.save(detail);

                }

                // Mark this investigation’s sample details as result entered
                dgSampleCollectionDetails.setResult_status("y");
                dgSampleCollectionDetailsRepository.save(dgSampleCollectionDetails);
            }

            //After all investigations processed
            updateResultEntryStatusIfComplete(request.getSampleCollectionHeaderId(), request.getSubChargeCodeId());
            log.info("Result entry saved/updated successfully for sampleCollectionHeaderId={}",
                    request.getSampleCollectionHeaderId());
            return ResponseUtils.createSuccessResponse("Result entry saved/updated successfully!", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error while saving result entry", e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Error saving result entry: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    public ApiResponse<List<DgResultEntryValidationResponse>> getUnvalidatedResults() {
        log.info("Starting getUnvalidatedResults()");
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            // Fetch headers with resultStatus = 'n' and details having validated = 'n'
            List<DgResultEntryHeader> headerList = headerRepo.findAllUnvalidatedHeaders();
            log.info("Found {} unvalidated result headers", headerList.size());

            List<DgResultEntryValidationResponse> responseList = new ArrayList<>();

            for (DgResultEntryHeader header : headerList) {
                log.debug("Processing ResultEntryHeader id={}", header.getResultEntryId());
                DgResultEntryValidationResponse headerDto = new DgResultEntryValidationResponse();
                String fullName = Stream.of(
                                header.getHinId().getPatientFn(),
                                header.getHinId().getPatientMn(),
                                header.getHinId().getPatientLn()
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));

                // ===== Header-level mapping =====
                headerDto.setResultEntryHeaderId(header.getResultEntryId());
                headerDto.setOrderDate(header.getOrderHd() != null && header.getOrderHd().getOrderDate() != null
                        ? header.getOrderHd().getOrderDate().toString() : null);
                headerDto.setResultTime(header.getResultTime());
                headerDto.setResultDate(header.getResultDate());
//                headerDto.setResultEntredBy(currentUser.getUsername());
                headerDto.setValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                headerDto.setEnteredBy(header.getLastChgdBy());
                headerDto.setPatientId(header.getHinId() != null ? header.getHinId().getId() : null);

                headerDto.setPatientName(fullName);
                // headerDto.setPatientName(header.getHinId() != null ? header.getHinId().getPatientFn() : null);
                headerDto.setRelationId(header.getRelationId().getId()!=null?header.getRelationId().getId():null);
                headerDto.setRelation(header.getRelationId().getId()!=null?header.getRelationId().getRelationName():null);
                headerDto.setPatientGender(header.getHinId() != null ? header.getHinId().getPatientGender().getGenderName() : null);
                headerDto.setPatientAge(header.getHinId() != null ? header.getHinId().getPatientAge() : null);
                headerDto.setPatientPhnNum(header.getHinId() != null ? header.getHinId().getPatientMobileNumber() : null);
                headerDto.setSubChargeCodeId(header.getSubChargeCodeId() != null ? header.getSubChargeCodeId().getSubId() : null);
                headerDto.setSubChargeCodeName(header.getSubChargeCodeId() != null ? header.getSubChargeCodeId().getSubName() : null);
                headerDto.setMainChargeCode(header.getMainChargecodeId() != null ? header.getMainChargecodeId().getChargecodeId() : null);
                headerDto.setMainChargeCodeName(header.getMainChargecodeId() != null ? header.getMainChargecodeId().getChargecodeCode().toUpperCase() : null);
                headerDto.setResultEnteredBy(header.getResultEnteredBy());
                headerDto.setOrderHdId((long) header.getOrderHd().getId());
                headerDto.setOrderNo(header.getOrderHd().getOrderNo());

                // ===== Detail-level mapping =====
                List<DgResultEntryDetail> detailList = detailRepo.findByResultEntryIdAndValidated(header, "n");
                log.debug("Header id={} has {} unvalidated details",
                        header.getResultEntryId(), detailList.size());

                // Group details by Investigation
                Map<Long, List<DgResultEntryDetail>> investigationMap = detailList.stream()
                        .filter(d -> d.getInvestigationId() != null)
                        .collect(Collectors.groupingBy(d -> d.getInvestigationId().getInvestigationId()));

                List<ResultEntryInvestigationResponse> investigationResponseList = new ArrayList<>();

                for (Map.Entry<Long, List<DgResultEntryDetail>> entry : investigationMap.entrySet()) {
                    Long investigationId = entry.getKey();
                    List<DgResultEntryDetail> subDetails = entry.getValue();
                    DgMasInvestigation inv = subDetails.get(0).getInvestigationId();
                    log.debug("Processing investigationId={} with {} records",
                            investigationId, subDetails.size());

                    ResultEntryInvestigationResponse invDto = new ResultEntryInvestigationResponse();
                    invDto.setInvestigationId(investigationId);
                    invDto.setInvestigationName(inv != null ? inv.getInvestigationName() : null);
                    invDto.setSampleName(inv != null && inv.getSampleId() != null ? inv.getSampleId().getSampleDescription() : null);

                    // Always show main investigation (even if no sub-investigation)
                    DgResultEntryDetail firstDetail = subDetails.get(0);
                    invDto.setResultEntryDetailsId(firstDetail.getResultEntryDetailId());
                    invDto.setResult(firstDetail.getResult());
                    invDto.setRemarks(firstDetail.getRemarks());
                    invDto.setNormalValue(firstDetail.getNormalRange());
                    invDto.setUnit(firstDetail.getUomId() != null ? firstDetail.getUomId().getName() : null);
                    invDto.setInRange(isResultWithinRange(firstDetail.getResult(), firstDetail.getNormalRange()));
                    invDto.setGeneratedSampleId(firstDetail.getGeneratedSampleId());

                    // ===== Sub-Investigation info (if present) =====
                    List<ResultEntrySubInvestigationRes> subList = new ArrayList<>();
                    for (DgResultEntryDetail sub : subDetails) {
                        if (sub.getSubInvestigationId() != null) {
                            ResultEntrySubInvestigationRes subDto = new ResultEntrySubInvestigationRes();
                            subDto.setResultEntryDetailsId(sub.getResultEntryDetailId());
                            subDto.setSubInvestigationId(sub.getSubInvestigationId().getSubInvestigationId());
                            subDto.setSubInvestigationName(sub.getSubInvestigationId().getSubInvestigationName());
                            subDto.setSampleName(sub.getSampleId() != null ? sub.getSampleId().getSampleDescription() : null);
                            subDto.setUnit(sub.getUomId() != null ? sub.getUomId().getName() : null);
                            subDto.setNormalValue(sub.getNormalRange());
                            subDto.setResult(sub.getResult());
                            subDto.setRemarks(sub.getRemarks());
                            String comparisonType = sub.getSubInvestigationId().getComparisonType();
                            log.info("comparisonType : {}",comparisonType);
                            if("f".equalsIgnoreCase(sub.getSubInvestigationId().getComparisonType())){
                                subDto.setComparisonType(sub.getSubInvestigationId().getComparisonType());
                                subDto.setFixedId(sub.getFixedId().getFixedId());
                            }
                            List<DgFixedValue> fixedDropdownValues = dgFixedValueRepository.findBySubInvestigationId(sub.getSubInvestigationId());
                            subDto.setFixedDropdownValues(fixedDropdownValues.stream().map(this::mapToDgFixedValueResponse).toList());
                            subDto.setInRange(isResultWithinRange(sub.getResult(), sub.getNormalRange()));
                            subDto.setGeneratedSampleId(sub.getGeneratedSampleId());
                            subList.add(subDto);
                        }
                    }

                    // keep empty list ([]) instead of null for frontend convenience
                    invDto.setResultEntrySubInvestigationRes(subList);

                    investigationResponseList.add(invDto);
                }

                headerDto.setResultEntryInvestigationResponses(investigationResponseList);
                responseList.add(headerDto);
            }
            log.info("Successfully prepared {} unvalidated result records",
                    responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error occurred while fetching unvalidated results", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    @Transactional
    public ApiResponse<String> updateResultValidation( ResultValidationUpdateRequest request) {
        log.info("Starting result validation. HeaderId={}",
                request.getResultEntryHeaderId());

        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access attempt. User not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            //For Date Time Formating
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");


            // 🔹 Step 1: Fetch header
            Optional<DgResultEntryHeader> optionalHeader = headerRepo.findById(request.getResultEntryHeaderId());
            if (optionalHeader.isEmpty()) {
                log.warn("Result entry header not found. HeaderId={}",
                        request.getResultEntryHeaderId());

                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Result entry header not found", HttpStatus.NOT_FOUND.value());
            }

            DgResultEntryHeader header = optionalHeader.get();
            log.info("Header fetched successfully. HeaderId={}",
                    header.getResultEntryId());
//            System.out.println("header = " + header);

            // 🔹 Step 2: Loop through validation list and update details
            for (ResultEntryValidationRequest validationReq : request.getValidationList()) {
                log.debug("Processing detailId={}",
                        validationReq.getResultEntryDetailsId());
                Optional<DgResultEntryDetail> optionalDetail = detailRepo.findById(validationReq.getResultEntryDetailsId());
                if (optionalDetail.isEmpty()) continue;

                DgResultEntryDetail detail = optionalDetail.get();

                // Update result and remarks
                detail.setResult(validationReq.getResult());
                detail.setRemarks(validationReq.getRemarks());
                if("f".equalsIgnoreCase(validationReq.getComparisonType())){
                    detail.setFixedId(dgFixedValueRepository.findById(validationReq.getFixedId()).orElse(null));
                }

                // Set validated status
                if (Boolean.TRUE.equals(validationReq.getValidated())) {
                    detail.setValidated("y");
                }

                //save order status in dgOrderDt
                DgOrderDt dgOrderDt = dgOrderDtRepository.findByOrderhdId_IdAndInvestigationId_InvestigationId(header.getOrderHd().getId(), detail.getInvestigationId().getInvestigationId());
                DgOrderDt byId = dgOrderDtRepository.findById(dgOrderDt.getId()).orElseThrow(() -> new RuntimeException("Invalid Dg Order Dt Id"));
                byId.setOrderTrackingStatus(orderTrackingStatusRepository.findById(resultValidatedStatusId).orElseThrow());
                dgOrderDtRepository.save(byId);

                // Save each detail
                LabTurnAroundTime labTurnAroundTime=labTurnAroundTimeRepository.findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(header.getOrderHd().getId(),detail.getInvestigationId().getInvestigationId(),header.getHinId().getId(),false);
                labTurnAroundTime.setResultValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labTurnAroundTime.setResultValidationTime(LocalDateTime.now());
                labTurnAroundTimeRepository.save(labTurnAroundTime);
                detailRepo.save(detail);
            }

            // 🔹 Step 3: Check if all details are validated
            List<DgResultEntryDetail> allDetails = detailRepo.findByResultEntryId(header);
            boolean allValidated = allDetails.stream()
                    .allMatch(d -> "y".equalsIgnoreCase(d.getValidated()));

            // 🔹 Step 4: Update header if all details validated
            if (allValidated) {
                log.info("All details validated. Updating header status.");
                header.setResultStatus("y"); // All validated
                // header.setVerified("y");
                header.setVerifiedOn(LocalDate.now());
                header.setVerifiedTime(LocalTime.now().format(formatter));
                header.setResultVerifiedBy(Math.toIntExact(currentUser.getUserId()));
                // header.setResultUpdatedBy(currentUser.getUsername());
                //  header.setUpdateOn(LocalDateTime.now());
                headerRepo.save(header);
            }
            log.info("Result validation completed successfully. HeaderId={}",
                    request.getResultEntryHeaderId());

            return ResponseUtils.createSuccessResponse(
                    "Result entry validation updated successfully",
                    new TypeReference<String>() {
                    });

        } catch (Exception e) {
            log.error("Error while validating result entry. HeaderId={}",
                    request.getResultEntryHeaderId(), e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error while validating result entry: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<ResultEntryUpdateResponse>> getUpdate() {
//
//        try {
//            User currentUser = authUtil.getCurrentUser();
//            if (currentUser == null) {
//                return ResponseUtils.createFailureResponse(
//                        null, new TypeReference<>() {},
//                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
//            }
//
//            // Step 1: Fetch all headers
//            List<DgResultEntryHeader> headerList = headerRepo.findAll();
//            if (headerList.isEmpty()) {
//                return ResponseUtils.createSuccessResponse(Collections.emptyList(), new TypeReference<>() {});
//            }
//
//            // Step 2: Group headers by OrderHdId and sort descending
//            Map<Long, List<DgResultEntryHeader>> groupedByOrder = headerList.stream()
//                    .filter(h -> h.getOrderHd() != null)
//                    .collect(Collectors.groupingBy(h -> (long) h.getOrderHd().getId()));
//
//            Map<Long, List<DgResultEntryHeader>> sortedGroupedByOrder = groupedByOrder.entrySet().stream()
//                    .sorted(Map.Entry.<Long, List<DgResultEntryHeader>>comparingByKey().reversed())
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            Map.Entry::getValue,
//                            (a, b) -> a,
//                            LinkedHashMap::new
//                    ));
//
//            List<ResultEntryUpdateResponse> responseList = new ArrayList<>();
//
//            // Step 3: For each Order
//            for (Map.Entry<Long, List<DgResultEntryHeader>> orderEntry : sortedGroupedByOrder.entrySet()) {
//                Long orderHdId = orderEntry.getKey();
//                List<DgResultEntryHeader> headersForOrder = orderEntry.getValue();
//
//                DgOrderHd order = headersForOrder.get(0).getOrderHd();
//                ResultEntryUpdateResponse orderResponse = new ResultEntryUpdateResponse();
//
//                orderResponse.setOrderHdId(orderHdId);
//                orderResponse.setOrderNo(order.getOrderNo());
//                orderResponse.setOrderDate(String.valueOf(order.getOrderDate()));
//                orderResponse.setOrderTime(order.getLastChgTime());
//
//                // Patient Info
//                DgResultEntryHeader firstHeader = headersForOrder.get(0);
//                if (firstHeader.getHinId() != null) {
//                    orderResponse.setPatientId(firstHeader.getHinId().getId());
//                    String fullName = Stream.of(
//                                    firstHeader.getHinId().getPatientFn(),
//                                    firstHeader.getHinId().getPatientMn(),
//                                    firstHeader.getHinId().getPatientLn())
//                            .filter(Objects::nonNull)
//                            .collect(Collectors.joining(" "));
//                    orderResponse.setPatientName(fullName);
//                    orderResponse.setPatientGender(firstHeader.getHinId().getPatientGender().getGenderName());
//                    orderResponse.setPatientAge(firstHeader.getHinId().getPatientAge());
//                    orderResponse.setPatientPhnNum(firstHeader.getHinId().getPatientMobileNumber());
//                }
//
//                if (firstHeader.getRelationId() != null) {
//                    orderResponse.setRelationId(firstHeader.getRelationId().getId());
//                    orderResponse.setRelation(firstHeader.getRelationId().getRelationName());
//                }
//
//                // Step 4: For each header under this order
//                List<ResultEntryUpdateHeaderResponse> headerResponses = new ArrayList<>();
//
//                for (DgResultEntryHeader header : headersForOrder) {
//                    ResultEntryUpdateHeaderResponse headerDto = new ResultEntryUpdateHeaderResponse();
//                    headerDto.setResultEntryHeaderId(header.getResultEntryId());
//
//                    // Only consider validated headers (y/n)
//                    if (!"y".equalsIgnoreCase(header.getResultStatus()) &&
//                            !"n".equalsIgnoreCase(header.getResultStatus())) {
//                        continue;
//                    }
//
//                    // Step 5: Fetch validated details for header
//                    List<DgResultEntryDetail> details = detailRepo.findValidatedDetailsByHeader(header);
//                    if (details.isEmpty()) continue;
//
//                    // Step 6: Group details by Investigation
//                    Map<Long, List<DgResultEntryDetail>> investigationMap = details.stream()
//                            .filter(d -> d.getInvestigationId() != null)
//                            .collect(Collectors.groupingBy(d -> d.getInvestigationId().getInvestigationId()));
//
//                    List<ResultEntryUpdateInvestigationResponse> investigationResponses = new ArrayList<>();
//
//                    for (Map.Entry<Long, List<DgResultEntryDetail>> invEntry : investigationMap.entrySet()) {
//                        List<DgResultEntryDetail> invDetails = invEntry.getValue();
//                        DgMasInvestigation inv = invDetails.get(0).getInvestigationId();
//
//                        ResultEntryUpdateInvestigationResponse invDto = new ResultEntryUpdateInvestigationResponse();
//                        invDto.setInvestigationId(inv.getInvestigationId());
//                        invDto.setInvestigationName(inv.getInvestigationName());
//                        invDto.setSampleName(inv.getSampleId() != null ? inv.getSampleId().getSampleDescription() : null);
//
//                        DgResultEntryDetail firstDetail = invDetails.get(0);
//                        invDto.setResultEntryDetailsId(firstDetail.getResultEntryDetailId());
//                        invDto.setResult(firstDetail.getResult());
//                        invDto.setRemarks(firstDetail.getRemarks());
//                        invDto.setNormalValue(firstDetail.getNormalRange());
//                        invDto.setUnit(firstDetail.getUomId() != null ? firstDetail.getUomId().getName() : null);
//                        invDto.setInRange(isResultWithinRange(firstDetail.getResult(), firstDetail.getNormalRange()));
//
//                        // Step 7: Sub Investigations
//                        List<ResultEntryUpdateSubInvestigationResponse> subList = new ArrayList<>();
//                        for (DgResultEntryDetail sub : invDetails) {
//                            if (sub.getSubInvestigationId() != null) {
//                                ResultEntryUpdateSubInvestigationResponse subDto = new ResultEntryUpdateSubInvestigationResponse();
//                                subDto.setResultEntryDetailsId(sub.getResultEntryDetailId());
//                                subDto.setSubInvestigationId(sub.getSubInvestigationId().getSubInvestigationId());
//                                subDto.setSubInvestigationName(sub.getSubInvestigationId().getSubInvestigationName());
//                                subDto.setSampleName(sub.getSampleId() != null ? sub.getSampleId().getSampleDescription() : null);
//                                subDto.setUnit(sub.getUomId() != null ? sub.getUomId().getName() : null);
//                                subDto.setNormalValue(sub.getNormalRange());
//                                subDto.setResult(sub.getResult());
//                                subDto.setRemarks(sub.getRemarks());
//                                subDto.setInRange(isResultWithinRange(sub.getResult(), sub.getNormalRange()));
//
//                                String comparisonType = sub.getSubInvestigationId().getComparisonType();
//                                if ("f".equalsIgnoreCase(comparisonType)) {
//                                    subDto.setComparisonType(comparisonType);
//                                    subDto.setFixedId(sub.getFixedId() != null ? sub.getFixedId().getFixedId() : null);
//
//                                    List<DgFixedValue> fixedDropdownValues =
//                                            dgFixedValueRepository.findBySubInvestigationId(sub.getSubInvestigationId());
//                                    subDto.setFixedDropdownValues(
//                                            fixedDropdownValues.stream()
//                                                    .map(this::mapToDgFixedValueResponse)
//                                                    .toList());
//                                }
//                                subList.add(subDto);
//                            }
//                        }
//
//                        invDto.setEntryUpdateSubInvestigationResponses(subList);
//                        investigationResponses.add(invDto);
//                    }
//
//                    headerDto.setResultEntryUpdateInvestigationResponseList(investigationResponses);
//                    headerResponses.add(headerDto);
//                }
//
//                orderResponse.setResultEntryUpdateHeaderResponses(headerResponses);
//                responseList.add(orderResponse);
//            }
//
//            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseUtils.createFailureResponse(
//                    null, new TypeReference<>() {}, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
        log.info("Starting getUpdate service");

        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            log.debug("Current user: {}", currentUser.getUsername());
            // Step 1: Fetch all headers
            // List<DgResultEntryHeader> headerList = headerRepo.findAll();
            List<DgResultEntryHeader> headerList = headerRepo.findAllByOrderByLastChgdDateDescLastChgdTimeDesc();
            log.info("Headers fetched from DB: count={}", headerList.size());
            if (headerList.isEmpty()) {
                log.info("No result entry headers found");
                return ResponseUtils.createSuccessResponse(Collections.emptyList(), new TypeReference<>() {});
            }

            // Step 2: Group headers by OrderHdId and sort descending
            Map<Long, List<DgResultEntryHeader>> groupedByOrder = headerList.stream()
                    .filter(h -> h.getOrderHd() != null)
                    .collect(Collectors.groupingBy(h -> (long) h.getOrderHd().getId()));
            log.debug("Grouped headers by order count={}", groupedByOrder.size());

            Map<Long, List<DgResultEntryHeader>> sortedGroupedByOrder = groupedByOrder.entrySet().stream()
                    .sorted(Map.Entry.<Long, List<DgResultEntryHeader>>comparingByKey().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            List<ResultEntryUpdateResponse> responseList = new ArrayList<>();

            // Step 3: For each Order
            for (Map.Entry<Long, List<DgResultEntryHeader>> orderEntry : sortedGroupedByOrder.entrySet()) {
                Long orderHdId = orderEntry.getKey();
                List<DgResultEntryHeader> headersForOrder = orderEntry.getValue();
                log.info("Processing OrderHdId={}, HeaderCount={}",
                        orderHdId, headersForOrder.size());


                DgOrderHd order = headersForOrder.get(0).getOrderHd();
                ResultEntryUpdateResponse orderResponse = new ResultEntryUpdateResponse();

                orderResponse.setOrderHdId(orderHdId);
                orderResponse.setOrderNo(order.getOrderNo());
                orderResponse.setOrderDate(String.valueOf(order.getOrderDate()));
                log.info("Order Time from Db : {}",order.getOrderTime());
                orderResponse.setOrderTime(getCurrentTimeFormatted(order.getOrderTime()));
                log.info("After formating Order Time : {}",getCurrentTimeFormatted(order.getOrderTime()));

                // Patient Info
                DgResultEntryHeader firstHeader = headersForOrder.get(0);
                if (firstHeader.getHinId() != null) {
                    orderResponse.setPatientId(firstHeader.getHinId().getId());
                    String fullName = Stream.of(
                                    firstHeader.getHinId().getPatientFn(),
                                    firstHeader.getHinId().getPatientMn(),
                                    firstHeader.getHinId().getPatientLn())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));
                    orderResponse.setPatientName(fullName);
                    orderResponse.setPatientGender(firstHeader.getHinId().getPatientGender().getGenderName());
                    orderResponse.setPatientAge(firstHeader.getHinId().getPatientAge());
                    orderResponse.setPatientPhnNum(firstHeader.getHinId().getPatientMobileNumber());
                }

                if (firstHeader.getRelationId() != null) {
                    orderResponse.setRelationId(firstHeader.getRelationId().getId());
                    orderResponse.setRelation(firstHeader.getRelationId().getRelationName());
                }

                // Step 4: For each header under this order
                List<ResultEntryUpdateHeaderResponse> headerResponses = new ArrayList<>();

                for (DgResultEntryHeader header : headersForOrder) {

                    // ❗ NEW LOGIC: Check if header contains at least one validated (status = 'y') detail
                    List<DgResultEntryDetail> allDetails = detailRepo.findByResultEntryId(header);
                    boolean hasValidatedDetail = allDetails.stream()
                            .anyMatch(d -> "y".equalsIgnoreCase(d.getValidated()));

                    if (!hasValidatedDetail) {
                        log.debug("Skipping headerId={} (no validated details)",
                                header.getResultEntryId());
                        continue; // Skip this header
                    }

                    // Now header is valid → proceed normally
                    ResultEntryUpdateHeaderResponse headerDto = new ResultEntryUpdateHeaderResponse();
                    headerDto.setResultEntryHeaderId(header.getResultEntryId());

                    // Step 5: Fetch only validated details
                    List<DgResultEntryDetail> details = detailRepo.findValidatedDetailsByHeader(header);
                    if (details.isEmpty()) continue;

                    // Step 6: Group details by Investigation
                    Map<Long, List<DgResultEntryDetail>> investigationMap = details.stream()
                            .filter(d -> d.getInvestigationId() != null)
                            .collect(Collectors.groupingBy(d -> d.getInvestigationId().getInvestigationId()));

                    List<ResultEntryUpdateInvestigationResponse> investigationResponses = new ArrayList<>();

                    for (Map.Entry<Long, List<DgResultEntryDetail>> invEntry : investigationMap.entrySet()) {
                        List<DgResultEntryDetail> invDetails = invEntry.getValue();
                        DgMasInvestigation inv = invDetails.get(0).getInvestigationId();

                        ResultEntryUpdateInvestigationResponse invDto = new ResultEntryUpdateInvestigationResponse();
                        invDto.setInvestigationId(inv.getInvestigationId());
                        invDto.setInvestigationName(inv.getInvestigationName());
                        invDto.setSampleName(inv.getSampleId() != null ? inv.getSampleId().getSampleDescription() : null);

                        DgResultEntryDetail firstDetail = invDetails.get(0);
                        invDto.setResultEntryDetailsId(firstDetail.getResultEntryDetailId());
                        invDto.setResult(firstDetail.getResult());
//                        invDto.setRemarks(firstDetail.getRemarks());
                        invDto.setNormalValue(firstDetail.getNormalRange());
                        invDto.setUnit(firstDetail.getUomId() != null ? firstDetail.getUomId().getName() : null);
                        invDto.setInRange(isResultWithinRange(firstDetail.getResult(), firstDetail.getNormalRange()));
                        invDto.setGeneratedSampleId(firstDetail.getGeneratedSampleId());
                        // Step 7: Sub Investigations
                        List<ResultEntryUpdateSubInvestigationResponse> subList = new ArrayList<>();
                        for (DgResultEntryDetail sub : invDetails) {
                            if (sub.getSubInvestigationId() != null) {
                                ResultEntryUpdateSubInvestigationResponse subDto = new ResultEntryUpdateSubInvestigationResponse();
                                subDto.setResultEntryDetailsId(sub.getResultEntryDetailId());
                                subDto.setSubInvestigationId(sub.getSubInvestigationId().getSubInvestigationId());
                                subDto.setSubInvestigationName(sub.getSubInvestigationId().getSubInvestigationName());
                                subDto.setSampleName(sub.getSampleId() != null ? sub.getSampleId().getSampleDescription() : null);
                                subDto.setUnit(sub.getUomId() != null ? sub.getUomId().getName() : null);
                                subDto.setNormalValue(sub.getNormalRange());
                                subDto.setResult(sub.getResult());
                                subDto.setRemarks(sub.getRemarks());
                                subDto.setInRange(isResultWithinRange(sub.getResult(), sub.getNormalRange()));
                                subDto.setGeneratedSampleId(sub.getGeneratedSampleId());
                                String comparisonType = sub.getSubInvestigationId().getComparisonType();
                                if ("f".equalsIgnoreCase(comparisonType)) {
                                    subDto.setComparisonType(comparisonType);
                                    subDto.setFixedId(sub.getFixedId() != null ? sub.getFixedId().getFixedId() : null);

                                    List<DgFixedValue> fixedDropdownValues =
                                            dgFixedValueRepository.findBySubInvestigationId(sub.getSubInvestigationId());
                                    subDto.setFixedDropdownValues(
                                            fixedDropdownValues.stream()
                                                    .map(this::mapToDgFixedValueResponse)
                                                    .toList());
                                }
                                subList.add(subDto);
                            }
                        }

                        invDto.setEntryUpdateSubInvestigationResponses(subList);
                        investigationResponses.add(invDto);
                    }

                    headerDto.setResultEntryUpdateInvestigationResponseList(investigationResponses);
                    headerResponses.add(headerDto);
                }

                orderResponse.setResultEntryUpdateHeaderResponses(headerResponses);

                // Add only if headers exist
                if (!headerResponses.isEmpty()) {
                    responseList.add(orderResponse);
                }
            }
            log.info("getUpdate service completed successfully. OrderCount={}",
                    responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching update result entries", e);

            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    @Transactional
    public ApiResponse<String> updateResult(ResultUpdateRequest request) {
        log.info("Starting updateResult process for HeaderId={}", request.getResultEntryHeaderId());
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            log.debug("Current user fetched successfully. UserId={}", currentUser.getUserId());

            // Fetch header
            Optional<DgResultEntryHeader> optionalHeader = headerRepo.findById(request.getResultEntryHeaderId());
            if (optionalHeader.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Header not found", HttpStatus.NOT_FOUND.value());
            }
            DgResultEntryHeader header = optionalHeader.get();
            log.info("Result Entry Header found. EntryId={}", header.getResultEntryId());
            // Update all details
            for (ResultUpdateDetailRequest detailReq : request.getResultUpdateDetailRequests()) {
                log.debug("Processing DetailId={}", detailReq.getResultEntryDetailsId());
                Optional<DgResultEntryDetail> optionalDetail = detailRepo.findById(detailReq.getResultEntryDetailsId());
                if (optionalDetail.isEmpty()) continue;
                DgResultEntryDetail detail = optionalDetail.get();
                if (!Objects.equals(detail.getResultEntryId().getResultEntryId(), header.getResultEntryId())) {
                    continue; // Skip if detail not under this header
                }

                //Update lab_result_amend_audit detail
                LabResultAmendAudit labResultAmendAudit= new LabResultAmendAudit();
                MasLabResultAmendmentType masLabResultAmendmentType = masLabResultAmendmentTypeRepository.findById(detailReq.getAmendmentTypeId()).orElseThrow(() -> new RuntimeException("Invalid Lab Result Amendment Type Id"));

                labResultAmendAudit.setPatient(header.getHinId());
                labResultAmendAudit.setAmendmentType(masLabResultAmendmentType);
                labResultAmendAudit.setAmendedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labResultAmendAudit.setNewResult(detailReq.getResult());
                labResultAmendAudit.setOldResult(detailReq.getOldResult());
                labResultAmendAudit.setAmendedDatetime(LocalDateTime.now());
                labResultAmendAudit.setInvestigation(detail.getInvestigationId());
                labResultAmendAudit.setReasonForChange(masLabResultAmendmentType.getAmendmentTypeName());
                labResultAmendAudit.setGeneratedSampleId(detail.getGeneratedSampleId());
                labResultAmendAudit.setRemarks(detailReq.getRemarks());

                labResultAmendAuditRepository.save(labResultAmendAudit);
                log.info("Audit saved for DetailId={}", detail.getResultEntryDetailId());


                // Update result and remarks per detail
                detail.setResult(detailReq.getResult());
//                detail.setRemarks(detailReq.getRemarks());
                if("f".equalsIgnoreCase(detailReq.getComparisonType())){
                    detail.setFixedId(dgFixedValueRepository.findById(detailReq.getFixedId()).orElse(null));
                }
                detailRepo.save(detail);
            }
            //  Update header audit fields
            header.setResultUpdatedBy(Math.toIntExact(currentUser.getUserId()));  // Who updated
            header.setUpdateOn(LocalDateTime.now());           // When updated
            headerRepo.save(header);
            log.info("Result update completed successfully for HeaderId={}",
                    header.getResultEntryId());

            return ResponseUtils.createSuccessResponse(
                    "Result and remarks updated successfully", new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Exception occurred while updating result. HeaderId={}",
                    request.getResultEntryHeaderId(), e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
    @Override
    public ApiResponse<List<ResultForInvestigationResponse>> getResultForInvestigation(Long patientId,Long hospitalId) {
        log.info("patientId={}, hospitalId={}", patientId, hospitalId);
        try {
            Optional<DgOrderHd> dgOrderHd = dgOrderHDRepository.findByPatientId_IdAndHospitalId(patientId, hospitalId);
            if (dgOrderHd.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {
                        }, "data not found",
                        404);
            }
            Optional<DgResultEntryHeader> dgResultEntryHeader = headerRepo.findByOrderHd_IdAndHospitalId_Id((long) dgOrderHd.get().getId(), hospitalId);
            Long resultEntryId = dgResultEntryHeader.get().getResultEntryId();
            List<DgResultEntryDetail> details = detailRepo.findByResultEntryId_ResultEntryIdAndValidatedIgnoreCase(resultEntryId, "y");
            List<ResultForInvestigationResponse> arr = details.stream()
                    .map(d -> {
                        ResultForInvestigationResponse response = new ResultForInvestigationResponse();
                        response.setPatientId(dgOrderHd.get().getPatientId().getId());
                        response.setPatientName(dgOrderHd.get().getPatientId().getFullName());
                        response.setAge(dgOrderHd.get().getPatientId().getPatientAge());
                        response.setInvestigationName(d.getInvestigationId().getInvestigationName());
                        response.setNormalRange(d.getNormalRange());
                        response.setResult(d.getResult());
                        return response;
                    })
                    .toList();
            log.info("getResultForInvestigation: success patientId={}, hospitalId={}, resultEntryId={}",
                    patientId, hospitalId, resultEntryId);
            return ResponseUtils.createSuccessResponse(arr, new TypeReference<>() {
            });
        }catch (Exception e) {
            log.error("getResultForInvestigation: error patientId={}, hospitalId={}", patientId, hospitalId, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "internal server error", 500
            );
        }
    }
    private void updateResultEntryStatusIfComplete(Long sampleHeaderId, Long subChargeCodeId) {
        List<DgSampleCollectionDetails> allDetails =
                dgSampleCollectionDetailsRepository
                        .findBySampleCollectionHeader_SampleCollectionHeaderIdAndSampleCollectionHeader_SubChargeCode_SubId(
                                sampleHeaderId, subChargeCodeId
                        );

        boolean allDone = allDetails.stream()
                .allMatch(d -> "y".equals(d.getResult_status()));

        if (allDone) {
            DgSampleCollectionHeader headerObj =
                    dgSampleCollectionHeaderRepository.findById(sampleHeaderId)
                            .orElseThrow(() -> new RuntimeException("Invalid Sample Header ID: " + sampleHeaderId));

            if ("n".equals(headerObj.getResult_entry_status())) {
                headerObj.setResult_entry_status("y");
                dgSampleCollectionHeaderRepository.save(headerObj);
            }
        }
    }

    private Boolean isResultWithinRange(String resultStr, String normalRangeStr) {
        if (resultStr == null || normalRangeStr == null) {
            return null; // no meaningful comparison possible
        }

        try {
            // Expecting normal range like "0.3 - 5.6"
            String[] parts = normalRangeStr.split("-");
            if (parts.length != 2) {
                return null; // invalid range format
            }

            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());
            double result = Double.parseDouble(resultStr.trim());

            return result >= min && result <= max;
        } catch (NumberFormatException e) {
            // Not numeric (like "Positive", "+", "++", "Trace")
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    private DgFixedValueResponse mapToDgFixedValueResponse(DgFixedValue entity){
        DgFixedValueResponse response= new DgFixedValueResponse();
        response.setFixedId(entity.getFixedId());
        response.setFixedValue(entity.getFixedValue());
        response.setSubInvestigationId(entity.getSubInvestigationId().getSubInvestigationId());
        return response;
    }


}
