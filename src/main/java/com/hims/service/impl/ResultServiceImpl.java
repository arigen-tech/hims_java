package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.ResultEntryInvestigationRequest;
import com.hims.request.ResultEntryMainRequest;
import com.hims.request.ResultEntrySubInvestigationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasSampleResponse;
import com.hims.service.ResultService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
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
    AuthUtil authUtil;

    @Override

    public ApiResponse<String> saveOrUpdateResultEntry(ResultEntryMainRequest request) {
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
                header.setDepartmentId(depObj);
                header.setLastChgdBy(currentUser.getLastChangedBy());
                header.setLastChgdDate(LocalDate.now());
                header.setLastChgdTime(String.valueOf(LocalTime.now()));
                header.setHospitalId(currentUser.getHospital());
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

                    DgSubMasInvestigation subInvestigation = dgSubMasInvestigationRepository.findById(subReq.getSubInvestigationId())
                            .orElseThrow(() -> new RuntimeException("Invalid SubInvestigation ID: " + subReq.getSubInvestigationId()));

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
                        detail.setSampleId(masSampleRepository.findById(subReq.getSampleId()).orElse(null));
                        detail.setChargeCodeId(mainChargeCodeRepository.findById(request.getMainChargeCodeId()).orElse(null));
                        detail.setUomId(subInvestigation.getUomId());
                       // detail.setNormalId(dgNormalValueRepository.findById(subReq.getNormalValueId()).orElse(null));
                     //  detail.setFixedId(dgFixedValueRepository.findById(subReq.getFixedValueId()).orElse(null));
             detail.setResultDetailStatus("n");
                    }
                    detailRepo.save(detail);
                }

                // Mark this investigation’s sample details as result entered
                dgSampleCollectionDetails.setResult_status("y");
                dgSampleCollectionDetailsRepository.save(dgSampleCollectionDetails);
            }

            //After all investigations processed ---
            Long subChargeCodeId = request.getSubChargeCodeId();
            Long sampleHeaderId = request.getSampleCollectionHeaderId();

            List<DgSampleCollectionDetails> allDetails =
                    dgSampleCollectionDetailsRepository.findBySampleCollectionHeader_SampleCollectionHeaderIdAndSampleCollectionHeader_SubChargeCode_SubId(
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

            return ResponseUtils.createSuccessResponse("Result entry saved/updated successfully!", new TypeReference<>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Error saving result entry: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    }
