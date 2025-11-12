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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



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
    AuthUtil authUtil;

    private final RandomNumGenerator randomNumGenerator;

    public  ResultServiceImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;

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
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            //  Check if header already exists for same Sample + SubChargeCode
            Optional<DgResultEntryHeader> existingHeaderOpt =
                    headerRepo.findBySampleCollectionHeaderId_SampleCollectionHeaderIdAndSubChargeCodeId_SubId(
                            request.getSampleCollectionHeaderId(),
                            request.getSubChargeCodeId()
                    );

            DgResultEntryHeader header;

            if (existingHeaderOpt.isPresent()) {
                // Update existing header
                header = existingHeaderOpt.get();
                header.setRemarks(request.getClinicalNotes());
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setResultStatus("n");
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
            } else {
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
//                Optional<DgOrderHd> dgOrderH=labHdRepository.findById(Math.toIntExact(request.getPatientId()));
//                header.setOrderHd(dgOrderH.get());
                Optional<DgOrderHd> dgOrderH = labHdRepository.findByPatientId_Id(request.getPatientId());

                DgOrderHd orderHd = dgOrderH.orElseThrow(() ->
                        new RuntimeException("No order found for patient ID: " + request.getPatientId()));

                header.setOrderHd(orderHd);
                header.setHinId(patientRepository.findById(request.getPatientId()).orElse(null));
                header.setMainChargecodeId(mainChargeCodeRepository.findById(request.getMainChargeCodeId()).orElse(null));
                header.setSubChargeCodeId(subChargeRepo.findById(request.getSubChargeCodeId()).orElse(null));
                header = headerRepo.save(header);
            }
            // Save or Update Details
            for (ResultEntryInvestigationRequest invReq : request.getInvestigationList()) {
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
                    }
                    detailRepo.save(detail);
                }

                // Mark this investigation’s sample details as result entered
                dgSampleCollectionDetails.setResult_status("y");
                dgSampleCollectionDetailsRepository.save(dgSampleCollectionDetails);
            }

            //After all investigations processed
            updateResultEntryStatusIfComplete(request.getSampleCollectionHeaderId(), request.getSubChargeCodeId());

            return ResponseUtils.createSuccessResponse("Result entry saved/updated successfully!", new TypeReference<>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Error saving result entry: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<DgResultEntryValidationResponse>> getUnvalidatedResults() {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            // Fetch headers with resultStatus = 'n' and details having validated = 'n'
            List<DgResultEntryHeader> headerList = headerRepo.findAllUnvalidatedHeaders();

            List<DgResultEntryValidationResponse> responseList = new ArrayList<>();

            for (DgResultEntryHeader header : headerList) {
                DgResultEntryValidationResponse headerDto = new DgResultEntryValidationResponse();

                // ===== Header-level mapping =====
                headerDto.setResultEntryHeaderId(header.getResultEntryId());
                headerDto.setOrderDate(header.getOrderHd() != null && header.getOrderHd().getOrderDate() != null
                        ? header.getOrderHd().getOrderDate().toString() : null);
                headerDto.setResultTime(header.getResultTime());
                headerDto.setResultDate(header.getResultDate());
                headerDto.setResultEntredBy(currentUser.getUsername());
                headerDto.setValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                headerDto.setEnteredBy(header.getLastChgdBy());
                headerDto.setPatientId(header.getHinId() != null ? header.getHinId().getId() : null);
                headerDto.setPatientName(header.getHinId() != null ? header.getHinId().getPatientFn() : null);
                headerDto.setRelationId(header.getRelationId().getId()!=null?header.getRelationId().getId():null);
               headerDto.setRelation(header.getRelationId().getId()!=null?header.getRelationId().getRelationName():null);
                headerDto.setPatientGender(header.getHinId() != null ? header.getHinId().getPatientGender().getGenderName() : null);
               headerDto.setPatientAge(header.getHinId() != null ? header.getHinId().getPatientAge() : null);
                headerDto.setPatientPhnNum(header.getHinId() != null ? header.getHinId().getPatientMobileNumber() : null);
                headerDto.setSubChargeCodeId(header.getSubChargeCodeId() != null ? header.getSubChargeCodeId().getSubId() : null);
                headerDto.setSubChargeCodeName(header.getSubChargeCodeId() != null ? header.getSubChargeCodeId().getSubName() : null);
               headerDto.setMainChargeCode(header.getMainChargecodeId() != null ? header.getMainChargecodeId().getChargecodeId() : null);


               // ===== Detail-level mapping =====
                List<DgResultEntryDetail> detailList = detailRepo.findByResultEntryIdAndValidated(header, "n");

                // Group details by Investigation
                Map<Long, List<DgResultEntryDetail>> investigationMap = detailList.stream()
                        .filter(d -> d.getInvestigationId() != null)
                        .collect(Collectors.groupingBy(d -> d.getInvestigationId().getInvestigationId()));

                List<ResultEntryInvestigationResponse> investigationResponseList = new ArrayList<>();

                for (Map.Entry<Long, List<DgResultEntryDetail>> entry : investigationMap.entrySet()) {
                    Long investigationId = entry.getKey();
                    List<DgResultEntryDetail> subDetails = entry.getValue();
                    DgMasInvestigation inv = subDetails.get(0).getInvestigationId();
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

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> updateResultValidation( ResultValidationUpdateRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            //For Date Time Formating
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");


            // 🔹 Step 1: Fetch header
            Optional<DgResultEntryHeader> optionalHeader = headerRepo.findById(request.getResultEntryHeaderId());
            if (optionalHeader.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Result entry header not found", HttpStatus.NOT_FOUND.value());
            }

            DgResultEntryHeader header = optionalHeader.get();
//            System.out.println("header = " + header);

            // 🔹 Step 2: Loop through validation list and update details
            for (ResultEntryValidationRequest validationReq : request.getValidationList()) {
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

                // Save each detail
                detailRepo.save(detail);
            }

            // 🔹 Step 3: Check if all details are validated
            List<DgResultEntryDetail> allDetails = detailRepo.findByResultEntryId(header);
            boolean allValidated = allDetails.stream()
                    .allMatch(d -> "y".equalsIgnoreCase(d.getValidated()));

            // 🔹 Step 4: Update header if all details validated
            if (allValidated) {
                header.setResultStatus("y"); // All validated
               // header.setVerified("y");
               header.setVerifiedOn(LocalDate.now());
                header.setVerifiedTime(LocalTime.now().format(formatter));
                header.setResultVerifiedBy(Math.toIntExact(currentUser.getUserId()));
              // header.setResultUpdatedBy(currentUser.getUsername());
              //  header.setUpdateOn(LocalDateTime.now());
                headerRepo.save(header);
            }

            return ResponseUtils.createSuccessResponse(
                    "Result entry validation updated successfully",
                    new TypeReference<String>() {
                    });

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error while validating result entry: " + e.getMessage(),
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
