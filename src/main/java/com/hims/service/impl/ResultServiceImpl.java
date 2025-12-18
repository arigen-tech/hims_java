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
    AuthUtil authUtil;

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
        try {
            Long depart = authUtil.getCurrentDepartmentId();
            MasDepartment depObj = masDepartmentRepository.getById(depart);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            log.info("Starting result entry save/update for sampleHeaderId={}, subChargeCodeId={}",
                    request.getSampleCollectionHeaderId(),
                    request.getSubChargeCodeId());

            Optional<DgResultEntryHeader> existingHeaderOpt =
                    headerRepo.findBySampleCollectionHeaderId_SampleCollectionHeaderIdAndSubChargeCodeId_SubId(
                            request.getSampleCollectionHeaderId(),
                            request.getSubChargeCodeId()
                    );

            DgResultEntryHeader header;
            DgOrderHd dgOrderH =
                    labHdRepository.findByPatientId_IdAndVisitId_Id(
                            request.getPatientId(),
                            request.getVisitId());

            Patient patientId =
                    patientRepository.findById(request.getPatientId()).orElse(null);

            if (existingHeaderOpt.isPresent()) {

                header = existingHeaderOpt.get();
                log.info("Updating existing result entry headerId={}",
                        header.getResultEntryId());

                header.setRemarks(request.getClinicalNotes());
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
                header.setResultStatus("n");

            } else {

                log.info("Creating new result entry header");

                header = new DgResultEntryHeader();
                header.setRelationId(
                        masRelationRepository
                                .findById(request.getRelationId())
                                .orElse(null));
                header.setRemarks(request.getClinicalNotes());
                header.setResultDate(LocalDate.now());
                header.setResultTime(String.valueOf(LocalTime.now()));

                DgSampleCollectionHeader dgSampleCollectionHeader =
                        dgSampleCollectionHeaderRepository
                                .findById(request.getSampleCollectionHeaderId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Invalid Sample Header ID: " +
                                                        request.getSampleCollectionHeaderId()));

                header.setSampleCollectionHeaderId(dgSampleCollectionHeader);
                header.setResultStatus("n");
                header.setDepartmentId(depObj);
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
                header.setResultNo(createInvoice());
                header.setHospitalId(currentUser.getHospital());
                header.setResultEnteredBy(
                        currentUser.getFirstName() + " " +
                                currentUser.getMiddleName() + " " +
                                currentUser.getLastName());
                header.setOrderHd(dgOrderH);
                header.setHinId(patientId);
                header.setMainChargecodeId(
                        mainChargeCodeRepository
                                .findById(request.getMainChargeCodeId())
                                .orElse(null));
                header.setSubChargeCodeId(
                        subChargeRepo
                                .findById(request.getSubChargeCodeId())
                                .orElse(null));

                header = headerRepo.save(header);
                log.info("New result entry header created with headerId={}",
                        header.getResultEntryId());
            }

            // ===== Save / Update Details =====
            for (ResultEntryInvestigationRequest invReq :
                    request.getInvestigationList()) {

                DgMasInvestigation investigation =
                        dgMasInvestigationRepository
                                .findById(invReq.getInvestigationId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Invalid Investigation ID: " +
                                                        invReq.getInvestigationId()));

                DgSampleCollectionDetails dgSampleCollectionDetails =
                        dgSampleCollectionDetailsRepository
                                .findById(invReq.getSampleCollectionDetailsId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Invalid sampleCollectionDetails ID: " +
                                                        invReq.getSampleCollectionDetailsId()));

                boolean anyResultEntered =
                        invReq.getResultEntryDetailsRequestList().stream()
                                .anyMatch(subReq ->
                                        subReq.getResult() != null &&
                                                !subReq.getResult().trim().isEmpty());

                if (!anyResultEntered) {
                    log.debug("Skipping investigationId={} (no results entered)",
                            invReq.getInvestigationId());
                    continue;
                }

                log.info("Processing investigationId={}",
                        invReq.getInvestigationId());

                for (ResultEntrySubInvestigationRequest subReq :
                        invReq.getResultEntryDetailsRequestList()) {

                    DgSubMasInvestigation subInvestigation = null;
                    if (subReq.getSubInvestigationId() != null) {
                        subInvestigation =
                                dgSubMasInvestigationRepository
                                        .findById(subReq.getSubInvestigationId())
                                        .orElse(null);
                    }

                    Optional<DgResultEntryDetail> existingDetailOpt =
                            detailRepo
                                    .findByResultEntryIdAndInvestigationIdAndSubInvestigationId(
                                            header,
                                            investigation,
                                            subInvestigation);

                    DgResultEntryDetail detail;

                    if (existingDetailOpt.isPresent()) {

                        detail = existingDetailOpt.get();
                        detail.setResult(subReq.getResult());
                        detail.setRemarks(subReq.getRemarks());

                    } else {

                        detail = new DgResultEntryDetail();
                        detail.setResultEntryId(header);
                        detail.setInvestigationId(investigation);
                        detail.setSubInvestigationId(subInvestigation);
                        detail.setSampleCollectionDetailsId(dgSampleCollectionDetails);
                        detail.setResult(subReq.getResult());
                        detail.setRemarks(subReq.getRemarks());
                        detail.setResultType(subReq.getResultType());
                        detail.setValidated("n");
                        detail.setSampleId(
                                masSampleRepository
                                        .findById(subReq.getSampleId())
                                        .orElse(null));
                        detail.setChargeCodeId(
                                mainChargeCodeRepository
                                        .findById(request.getMainChargeCodeId())
                                        .orElse(null));

                        if ("n".equalsIgnoreCase(subReq.getComparisonType())) {
                            detail.setNormalId(
                                    dgNormalValueRepository
                                            .findById(subReq.getNormalId())
                                            .orElse(null));
                        } else if ("f".equalsIgnoreCase(subReq.getComparisonType())) {
                            detail.setFixedId(
                                    dgFixedValueRepository
                                            .findById(subReq.getFixedId())
                                            .orElse(null));
                        }

                        detail.setNormalRange(subReq.getNormalRange());
                        detail.setFixedValue(subReq.getFixedValue());
                        detail.setUomId(
                                subInvestigation != null
                                        ? subInvestigation.getUomId()
                                        : investigation.getUomId());
                        detail.setResultDetailStatus("n");
                    }

                    LabTurnAroundTime labTurnAroundTime =
                            labTurnAroundTimeRepository
                                    .findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(
                                            dgOrderH.getId(),
                                            investigation.getInvestigationId(),
                                            patientId.getId(),
                                            false);

                    labTurnAroundTime.setResultEnteredBy(
                            currentUser.getFirstName() + " " +
                                    currentUser.getMiddleName() + " " +
                                    currentUser.getLastName());
                    labTurnAroundTime.setResultEntryDateTime(LocalDateTime.now());
                    labTurnAroundTimeRepository.save(labTurnAroundTime);

                    detailRepo.save(detail);
                }

                dgSampleCollectionDetails.setResult_status("y");
                dgSampleCollectionDetailsRepository.save(dgSampleCollectionDetails);
            }

            updateResultEntryStatusIfComplete(
                    request.getSampleCollectionHeaderId(),
                    request.getSubChargeCodeId());

            log.info("Result entry saved/updated successfully for sampleHeaderId={}",
                    request.getSampleCollectionHeaderId());

            return ResponseUtils.createSuccessResponse(
                    "Result entry saved/updated successfully!",
                    new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while saving result entry for sampleHeaderId={}",
                    request.getSampleCollectionHeaderId(), e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error saving result entry",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<DgResultEntryValidationResponse>> getUnvalidatedResults() {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            log.info("Fetching unvalidated result entry headers");

            // Fetch headers with resultStatus = 'n'
            List<DgResultEntryHeader> headerList =
                    headerRepo.findAllUnvalidatedHeaders();

            List<DgResultEntryValidationResponse> responseList =
                    new ArrayList<>();

            for (DgResultEntryHeader header : headerList) {

                log.info("Processing resultEntryHeaderId={}",
                        header.getResultEntryId());

                DgResultEntryValidationResponse headerDto =
                        new DgResultEntryValidationResponse();

                String fullName = Stream.of(
                                header.getHinId().getPatientFn(),
                                header.getHinId().getPatientMn(),
                                header.getHinId().getPatientLn())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));

                // ===== Header-level mapping =====
                headerDto.setResultEntryHeaderId(
                        header.getResultEntryId());
                headerDto.setOrderDate(
                        header.getOrderHd() != null &&
                                header.getOrderHd().getOrderDate() != null
                                ? header.getOrderHd().getOrderDate().toString()
                                : null);
                headerDto.setResultTime(header.getResultTime());
                headerDto.setResultDate(header.getResultDate());
                headerDto.setValidatedBy(
                        currentUser.getFirstName() + " " +
                                currentUser.getMiddleName() + " " +
                                currentUser.getLastName());
                headerDto.setEnteredBy(header.getLastChgdBy());
                headerDto.setPatientId(
                        header.getHinId() != null
                                ? header.getHinId().getId()
                                : null);

                headerDto.setPatientName(fullName);
                headerDto.setRelationId(
                        header.getRelationId() != null
                                ? header.getRelationId().getId()
                                : null);
                headerDto.setRelation(
                        header.getRelationId() != null
                                ? header.getRelationId().getRelationName()
                                : null);
                headerDto.setPatientGender(
                        header.getHinId() != null
                                ? header.getHinId()
                                .getPatientGender()
                                .getGenderName()
                                : null);
                headerDto.setPatientAge(
                        header.getHinId() != null
                                ? header.getHinId().getPatientAge()
                                : null);
                headerDto.setPatientPhnNum(
                        header.getHinId() != null
                                ? header.getHinId().getPatientMobileNumber()
                                : null);
                headerDto.setSubChargeCodeId(
                        header.getSubChargeCodeId() != null
                                ? header.getSubChargeCodeId().getSubId()
                                : null);
                headerDto.setSubChargeCodeName(
                        header.getSubChargeCodeId() != null
                                ? header.getSubChargeCodeId().getSubName()
                                : null);
                headerDto.setMainChargeCode(
                        header.getMainChargecodeId() != null
                                ? header.getMainChargecodeId().getChargecodeId()
                                : null);
                headerDto.setMainChargeCodeName(
                        header.getMainChargecodeId() != null
                                ? header.getMainChargecodeId()
                                .getChargecodeCode().toUpperCase()
                                : null);
                headerDto.setResultEnteredBy(
                        header.getResultEnteredBy());
                headerDto.setOrderHdId(
                        (long) header.getOrderHd().getId());

                // ===== Detail-level mapping =====
                List<DgResultEntryDetail> detailList =
                        detailRepo.findByResultEntryIdAndValidated(
                                header, "n");

                log.debug("Found {} unvalidated details for headerId={}",
                        detailList.size(),
                        header.getResultEntryId());

                Map<Long, List<DgResultEntryDetail>> investigationMap =
                        detailList.stream()
                                .filter(d -> d.getInvestigationId() != null)
                                .collect(Collectors.groupingBy(
                                        d -> d.getInvestigationId()
                                                .getInvestigationId()));

                List<ResultEntryInvestigationResponse>
                        investigationResponseList =
                        new ArrayList<>();

                for (Map.Entry<Long, List<DgResultEntryDetail>> entry :
                        investigationMap.entrySet()) {

                    List<DgResultEntryDetail> subDetails =
                            entry.getValue();
                    DgMasInvestigation inv =
                            subDetails.get(0).getInvestigationId();

                    log.debug("Processing investigationId={}, name={}",
                            entry.getKey(),
                            inv != null ? inv.getInvestigationName() : null);

                    ResultEntryInvestigationResponse invDto =
                            new ResultEntryInvestigationResponse();

                    invDto.setInvestigationId(entry.getKey());
                    invDto.setInvestigationName(
                            inv != null ? inv.getInvestigationName() : null);
                    invDto.setSampleName(
                            inv != null && inv.getSampleId() != null
                                    ? inv.getSampleId()
                                    .getSampleDescription()
                                    : null);

                    DgResultEntryDetail firstDetail =
                            subDetails.get(0);

                    invDto.setResultEntryDetailsId(
                            firstDetail.getResultEntryDetailId());
                    invDto.setResult(firstDetail.getResult());
                    invDto.setRemarks(firstDetail.getRemarks());
                    invDto.setNormalValue(firstDetail.getNormalRange());
                    invDto.setUnit(
                            firstDetail.getUomId() != null
                                    ? firstDetail.getUomId().getName()
                                    : null);
                    invDto.setInRange(
                            isResultWithinRange(
                                    firstDetail.getResult(),
                                    firstDetail.getNormalRange()));

                    List<ResultEntrySubInvestigationRes> subList =
                            new ArrayList<>();

                    for (DgResultEntryDetail sub : subDetails) {
                        if (sub.getSubInvestigationId() != null) {

                            String comparisonType =
                                    sub.getSubInvestigationId()
                                            .getComparisonType();

                            log.debug("SubInvestigationId={}, comparisonType={}",
                                    sub.getSubInvestigationId()
                                            .getSubInvestigationId(),
                                    comparisonType);

                            ResultEntrySubInvestigationRes subDto =
                                    new ResultEntrySubInvestigationRes();

                            subDto.setResultEntryDetailsId(
                                    sub.getResultEntryDetailId());
                            subDto.setSubInvestigationId(
                                    sub.getSubInvestigationId()
                                            .getSubInvestigationId());
                            subDto.setSubInvestigationName(
                                    sub.getSubInvestigationId()
                                            .getSubInvestigationName());
                            subDto.setSampleName(
                                    sub.getSampleId() != null
                                            ? sub.getSampleId()
                                            .getSampleDescription()
                                            : null);
                            subDto.setUnit(
                                    sub.getUomId() != null
                                            ? sub.getUomId().getName()
                                            : null);
                            subDto.setNormalValue(
                                    sub.getNormalRange());
                            subDto.setResult(sub.getResult());
                            subDto.setRemarks(sub.getRemarks());

                            if ("f".equalsIgnoreCase(comparisonType)) {
                                subDto.setComparisonType(comparisonType);
                                subDto.setFixedId(
                                        sub.getFixedId() != null
                                                ? sub.getFixedId().getFixedId()
                                                : null);
                            }

                            subDto.setFixedDropdownValues(
                                    dgFixedValueRepository
                                            .findBySubInvestigationId(
                                                    sub.getSubInvestigationId())
                                            .stream()
                                            .map(this::mapToDgFixedValueResponse)
                                            .toList());

                            subDto.setInRange(
                                    isResultWithinRange(
                                            sub.getResult(),
                                            sub.getNormalRange()));

                            subList.add(subDto);
                        }
                    }

                    invDto.setResultEntrySubInvestigationRes(subList);
                    investigationResponseList.add(invDto);
                }

                headerDto.setResultEntryInvestigationResponses(
                        investigationResponseList);

                responseList.add(headerDto);
            }

            log.info("Successfully fetched {} unvalidated result entries",
                    responseList.size());

            return ResponseUtils.createSuccessResponse(
                    responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching unvalidated result entries", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    @Transactional
    public ApiResponse<String> updateResultValidation( ResultValidationUpdateRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            log.info("Starting result validation for headerId={}",
                    request.getResultEntryHeaderId());

            // For Date Time Formatting
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            // 🔹 Step 1: Fetch header
            Optional<DgResultEntryHeader> optionalHeader =
                    headerRepo.findById(request.getResultEntryHeaderId());

            if (optionalHeader.isEmpty()) {
                log.warn("Result entry header not found for headerId={}",
                        request.getResultEntryHeaderId());
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Result entry header not found",
                        HttpStatus.NOT_FOUND.value());
            }

            DgResultEntryHeader header = optionalHeader.get();
            log.debug("Fetched result entry header: {}", header.getResultEntryId());

            // 🔹 Step 2: Loop through validation list and update details
            for (ResultEntryValidationRequest validationReq : request.getValidationList()) {

                Optional<DgResultEntryDetail> optionalDetail =
                        detailRepo.findById(validationReq.getResultEntryDetailsId());

                if (optionalDetail.isEmpty()) {
                    log.warn("Result entry detail not found for detailId={}",
                            validationReq.getResultEntryDetailsId());
                    continue;
                }

                DgResultEntryDetail detail = optionalDetail.get();

                // Update result and remarks
                detail.setResult(validationReq.getResult());
                detail.setRemarks(validationReq.getRemarks());

                if ("f".equalsIgnoreCase(validationReq.getComparisonType())) {
                    detail.setFixedId(
                            dgFixedValueRepository
                                    .findById(validationReq.getFixedId())
                                    .orElse(null));
                }

                // Set validated status
                if (Boolean.TRUE.equals(validationReq.getValidated())) {
                    detail.setValidated("y");
                }

                // Update Lab Turn Around Time
                LabTurnAroundTime labTurnAroundTime =
                        labTurnAroundTimeRepository
                                .findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(
                                        header.getOrderHd().getId(),
                                        detail.getInvestigationId().getInvestigationId(),
                                        header.getHinId().getId(),
                                        false);

                if (labTurnAroundTime != null) {
                    labTurnAroundTime.setResultValidatedBy(
                            currentUser.getFirstName() + " " +
                                    currentUser.getMiddleName() + " " +
                                    currentUser.getLastName());
                    labTurnAroundTime.setResultValidationTime(LocalDateTime.now());
                    labTurnAroundTimeRepository.save(labTurnAroundTime);

                    log.debug("Updated TAT for investigationId={}",
                            detail.getInvestigationId().getInvestigationId());
                }

                detailRepo.save(detail);
                log.debug("Validated result entry detailId={}",
                        detail.getResultEntryDetailId());
            }

            // 🔹 Step 3: Check if all details are validated
            List<DgResultEntryDetail> allDetails =
                    detailRepo.findByResultEntryId(header);

            boolean allValidated = allDetails.stream()
                    .allMatch(d -> "y".equalsIgnoreCase(d.getValidated()));

            log.info("All details validated status for headerId={} : {}",
                    header.getResultEntryId(), allValidated);

            // 🔹 Step 4: Update header if all details validated
            if (allValidated) {
                header.setResultStatus("y");
                header.setVerifiedOn(LocalDate.now());
                header.setVerifiedTime(LocalTime.now().format(formatter));
                header.setResultVerifiedBy(
                        Math.toIntExact(currentUser.getUserId()));

                headerRepo.save(header);
                log.info("Result entry header verified successfully for headerId={}",
                        header.getResultEntryId());
            }

            return ResponseUtils.createSuccessResponse(
                    "Result entry validation updated successfully",
                    new TypeReference<String>() {});

        } catch (Exception e) {
            log.error("Error while validating result entry for headerId={}",
                    request.getResultEntryHeaderId(), e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error while validating result entry",
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

        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            log.info("Fetching result entry headers");

            List<DgResultEntryHeader> headerList =
                    headerRepo.findAllByOrderByLastChgdDateDescLastChgdTimeDesc();

            if (headerList.isEmpty()) {
                log.info("No result entry headers found");
                return ResponseUtils.createSuccessResponse(
                        Collections.emptyList(), new TypeReference<>() {});
            }

            // Group headers by OrderHdId
            Map<Long, List<DgResultEntryHeader>> groupedByOrder = headerList.stream()
                    .filter(h -> h.getOrderHd() != null)
                    .collect(Collectors.groupingBy(h -> (long) h.getOrderHd().getId()));

            Map<Long, List<DgResultEntryHeader>> sortedGroupedByOrder =
                    groupedByOrder.entrySet().stream()
                            .sorted(Map.Entry.<Long, List<DgResultEntryHeader>>comparingByKey().reversed())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

            List<ResultEntryUpdateResponse> responseList = new ArrayList<>();

            for (Map.Entry<Long, List<DgResultEntryHeader>> orderEntry : sortedGroupedByOrder.entrySet()) {

                Long orderHdId = orderEntry.getKey();
                List<DgResultEntryHeader> headersForOrder = orderEntry.getValue();
                DgOrderHd order = headersForOrder.get(0).getOrderHd();

                log.info("Processing orderId={}, orderNo={}",
                        orderHdId, order.getOrderNo());

                ResultEntryUpdateResponse orderResponse = new ResultEntryUpdateResponse();
                orderResponse.setOrderHdId(orderHdId);
                orderResponse.setOrderNo(order.getOrderNo());
                orderResponse.setOrderDate(String.valueOf(order.getOrderDate()));

                log.debug("Order time from DB: {}", order.getOrderTime());
                orderResponse.setOrderTime(getCurrentTimeFormatted(order.getOrderTime()));
                log.debug("Formatted order time: {}", orderResponse.getOrderTime());

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
                    orderResponse.setPatientGender(
                            firstHeader.getHinId().getPatientGender().getGenderName());
                    orderResponse.setPatientAge(firstHeader.getHinId().getPatientAge());
                    orderResponse.setPatientPhnNum(
                            firstHeader.getHinId().getPatientMobileNumber());
                }

                if (firstHeader.getRelationId() != null) {
                    orderResponse.setRelationId(firstHeader.getRelationId().getId());
                    orderResponse.setRelation(firstHeader.getRelationId().getRelationName());
                }

                List<ResultEntryUpdateHeaderResponse> headerResponses = new ArrayList<>();

                for (DgResultEntryHeader header : headersForOrder) {

                    List<DgResultEntryDetail> allDetails =
                            detailRepo.findByResultEntryId(header);

                    boolean hasValidatedDetail = allDetails.stream()
                            .anyMatch(d -> "y".equalsIgnoreCase(d.getValidated()));

                    if (!hasValidatedDetail) {
                        log.debug("Skipping headerId={} as no validated details found",
                                header.getResultEntryId());
                        continue;
                    }

                    ResultEntryUpdateHeaderResponse headerDto =
                            new ResultEntryUpdateHeaderResponse();
                    headerDto.setResultEntryHeaderId(header.getResultEntryId());

                    List<DgResultEntryDetail> details =
                            detailRepo.findValidatedDetailsByHeader(header);

                    if (details.isEmpty()) {
                        log.debug("No validated details for headerId={}",
                                header.getResultEntryId());
                        continue;
                    }

                    Map<Long, List<DgResultEntryDetail>> investigationMap =
                            details.stream()
                                    .filter(d -> d.getInvestigationId() != null)
                                    .collect(Collectors.groupingBy(
                                            d -> d.getInvestigationId().getInvestigationId()));

                    List<ResultEntryUpdateInvestigationResponse> investigationResponses =
                            new ArrayList<>();

                    for (Map.Entry<Long, List<DgResultEntryDetail>> invEntry :
                            investigationMap.entrySet()) {

                        List<DgResultEntryDetail> invDetails = invEntry.getValue();
                        DgMasInvestigation inv = invDetails.get(0).getInvestigationId();

                        log.debug("Processing investigationId={}, name={}",
                                inv.getInvestigationId(), inv.getInvestigationName());

                        ResultEntryUpdateInvestigationResponse invDto =
                                new ResultEntryUpdateInvestigationResponse();

                        invDto.setInvestigationId(inv.getInvestigationId());
                        invDto.setInvestigationName(inv.getInvestigationName());
                        invDto.setSampleName(
                                inv.getSampleId() != null
                                        ? inv.getSampleId().getSampleDescription()
                                        : null);

                        DgResultEntryDetail firstDetail = invDetails.get(0);
                        invDto.setResultEntryDetailsId(firstDetail.getResultEntryDetailId());
                        invDto.setResult(firstDetail.getResult());
                        invDto.setRemarks(firstDetail.getRemarks());
                        invDto.setNormalValue(firstDetail.getNormalRange());
                        invDto.setUnit(firstDetail.getUomId() != null
                                ? firstDetail.getUomId().getName()
                                : null);
                        invDto.setInRange(
                                isResultWithinRange(
                                        firstDetail.getResult(),
                                        firstDetail.getNormalRange()));

                        List<ResultEntryUpdateSubInvestigationResponse> subList =
                                new ArrayList<>();

                        for (DgResultEntryDetail sub : invDetails) {
                            if (sub.getSubInvestigationId() != null) {

                                ResultEntryUpdateSubInvestigationResponse subDto =
                                        new ResultEntryUpdateSubInvestigationResponse();

                                subDto.setResultEntryDetailsId(
                                        sub.getResultEntryDetailId());
                                subDto.setSubInvestigationId(
                                        sub.getSubInvestigationId()
                                                .getSubInvestigationId());
                                subDto.setSubInvestigationName(
                                        sub.getSubInvestigationId()
                                                .getSubInvestigationName());
                                subDto.setSampleName(
                                        sub.getSampleId() != null
                                                ? sub.getSampleId()
                                                .getSampleDescription()
                                                : null);
                                subDto.setUnit(
                                        sub.getUomId() != null
                                                ? sub.getUomId().getName()
                                                : null);
                                subDto.setNormalValue(sub.getNormalRange());
                                subDto.setResult(sub.getResult());
                                subDto.setRemarks(sub.getRemarks());
                                subDto.setInRange(
                                        isResultWithinRange(
                                                sub.getResult(),
                                                sub.getNormalRange()));

                                String comparisonType =
                                        sub.getSubInvestigationId()
                                                .getComparisonType();

                                if ("f".equalsIgnoreCase(comparisonType)) {
                                    subDto.setComparisonType(comparisonType);
                                    subDto.setFixedId(
                                            sub.getFixedId() != null
                                                    ? sub.getFixedId().getFixedId()
                                                    : null);

                                    subDto.setFixedDropdownValues(
                                            dgFixedValueRepository
                                                    .findBySubInvestigationId(
                                                            sub.getSubInvestigationId())
                                                    .stream()
                                                    .map(this::mapToDgFixedValueResponse)
                                                    .toList());
                                }
                                subList.add(subDto);
                            }
                        }

                        invDto.setEntryUpdateSubInvestigationResponses(subList);
                        investigationResponses.add(invDto);
                    }

                    headerDto.setResultEntryUpdateInvestigationResponseList(
                            investigationResponses);
                    headerResponses.add(headerDto);
                }

                if (!headerResponses.isEmpty()) {
                    orderResponse.setResultEntryUpdateHeaderResponses(headerResponses);
                    responseList.add(orderResponse);
                }
            }

            log.info("Result update fetch completed successfully");
            return ResponseUtils.createSuccessResponse(
                    responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching result update data", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
    @Override
    @Transactional
    public ApiResponse<String> updateResult(ResultUpdateRequest request) {

        log.info("Update result request received for headerId: {}",
                request.getResultEntryHeaderId());

        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access while updating result");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            Optional<DgResultEntryHeader> optionalHeader =
                    headerRepo.findById(request.getResultEntryHeaderId());

            if (optionalHeader.isEmpty()) {
                log.warn("Result header not found for headerId: {}",
                        request.getResultEntryHeaderId());
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Header not found",
                        HttpStatus.NOT_FOUND.value());
            }

            DgResultEntryHeader header = optionalHeader.get();

            // Update all details
            for (ResultUpdateDetailRequest detailReq : request.getResultUpdateDetailRequests()) {

                Optional<DgResultEntryDetail> optionalDetail =
                        detailRepo.findById(detailReq.getResultEntryDetailsId());

                if (optionalDetail.isEmpty()) {
                    log.warn("Detail not found for detailId: {}",
                            detailReq.getResultEntryDetailsId());
                    continue;
                }

                DgResultEntryDetail detail = optionalDetail.get();

                if (!Objects.equals(
                        detail.getResultEntryId().getResultEntryId(),
                        header.getResultEntryId())) {

                    log.warn("Detail {} does not belong to header {}",
                            detailReq.getResultEntryDetailsId(),
                            header.getResultEntryId());
                    continue;
                }

                detail.setResult(detailReq.getResult());
                detail.setRemarks(detailReq.getRemarks());

                if ("f".equalsIgnoreCase(detailReq.getComparisonType())) {
                    detail.setFixedId(
                            dgFixedValueRepository
                                    .findById(detailReq.getFixedId())
                                    .orElse(null));
                }

                detailRepo.save(detail);
                log.debug("Updated detailId: {}",
                        detailReq.getResultEntryDetailsId());
            }

            // Update header audit fields
            header.setResultUpdatedBy(Math.toIntExact(currentUser.getUserId()));
            header.setUpdateOn(LocalDateTime.now());
            headerRepo.save(header);

            log.info("Result updated successfully for headerId: {}",
                    header.getResultEntryId());

            return ResponseUtils.createSuccessResponse(
                    "Result and remarks updated successfully",
                    new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while updating result for headerId: {}",
                    request.getResultEntryHeaderId(), e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
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
