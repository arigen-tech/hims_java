package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.InvestigationValidationRequest;
import com.hims.response.*;
import com.hims.service.SampleValidationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SampleValidationServiceImpl implements SampleValidationService {
    @Autowired
     DgSampleCollectionDetailsRepository detailsRepo;
    @Autowired
    DgSampleCollectionHeaderRepository headerRepo;
    @Autowired
    DgSubMasInvestigationRepository dgSubMasInvestigationRepository;
@Autowired
DgNormalValueRepository dgNormalValueRepository;
@Autowired
    VisitRepository visitRepository;
@Autowired
    LabHdRepository labHdRepository;
@Autowired
DgFixedValueRepository dgFixedValueRepository;
    @Autowired
    AuthUtil authUtil;

    @Autowired
    private MasSubChargeCodeRepository subChargeCodeRepository;



    @Override
    @Transactional
    public ApiResponse<String> validateInvestigations(List<InvestigationValidationRequest> requests) {
        try {
            log.info("Investigation validation Process Started..");
            for (InvestigationValidationRequest req : requests) {
                String validated = (req.getAccepted() != null && req.getAccepted()) ? "y" : "n";
                detailsRepo.updateValidationStatus(req.getDetailId(), validated);
            }

            // 2. Collect all involved headerIds
            List<Long> detailIds = requests.stream()
                    .map(InvestigationValidationRequest::getDetailId)
                    .collect(Collectors.toList());

            Set<Long> headerIds = detailsRepo.findHeaderIdsByDetailIds(detailIds);

            // 3. For each header, determine order status
            for (Long headerId : headerIds) {
                long total = detailsRepo.countTotalByHeaderId(headerId);
                long accepted = detailsRepo.countAcceptedByHeaderId(headerId);

                String orderStatus;
                if (accepted == total) {
                    orderStatus = "y"; // all accepted
                }
                else {
                    orderStatus = "n"; // all rejected (optional)
                }

                headerRepo.updateOrderStatus(headerId, orderStatus);
            }
            log.info("Investigation validation Process Ended..");
            return ResponseUtils.createSuccessResponse("investigation validated success", new TypeReference<String>() {});
        } catch (Exception e) {
           log.error("Sample Validate Error :: ",e);
           return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.BAD_REQUEST.value());
        }


    }
    @Override
    @Transactional
    public ApiResponse<List<SampleValidationResponse>> getInvestigationsWithOrderStatusNAndP() {
        try {
            log.info("Investigation status process started..");

            // Step 1: Fetch all valid details with joins
            List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllByHeaderValidatedStatusLogic();
            log.info("Fetched details count: {}", detailsList.size());

            // Step 2: Group by (patientId + headerId)
            Map<String, List<DgSampleCollectionDetails>> grouped = detailsList.stream()
                    .filter(d -> d.getSampleCollectionHeader() != null && d.getSampleCollectionHeader().getPatientId() != null)
                    .collect(Collectors.groupingBy(d -> {
                        DgSampleCollectionHeader h = d.getSampleCollectionHeader();
                        return h.getPatientId().getId() + "_" + h.getSampleCollectionHeaderId();
                    }));

            // Step 3: Convert each group into SampleValidationResponse
            List<SampleValidationResponse> responseList = grouped.entrySet().stream()
                    .map(entry -> {
                        List<DgSampleCollectionDetails> groupDetails = entry.getValue();
                        DgSampleCollectionHeader header = groupDetails.get(0).getSampleCollectionHeader();

                        var patient = header.getPatientId();
                        var subCharge = header.getSubChargeCode();

                        String fullName = Stream.of(
                                        patient.getPatientFn(),
                                        patient.getPatientMn(),
                                        patient.getPatientLn()
                                )
                                .filter(Objects::nonNull)
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.joining(" "));

                        // Map details to TestDetailsDTO
                        List<TestDetailsDTO> investigations = groupDetails.stream()
                                .map(d -> new TestDetailsDTO(
                                        d.getSampleCollectionDetailsId(),
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationId() : null,
                                        d.getInvestigationId() != null && d.getInvestigationId().getSampleId() != null
                                                ? d.getInvestigationId().getSampleId().getSampleCode()
                                                : null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                                        d.getSampleId() != null ? d.getSampleId().getId() : null,
                                        d.getSampleId() != null ? d.getSampleId().getSampleDescription() : null,
                                        d.getQuantity(),
                                        d.getEmpanelledStatus(),
                                        d.getSampleCollDatetime(),
                                        d.getRejected_reason(),
                                        d.getRemarks()
                                ))
                                .toList();
                        // Build final response (each header = separate entry)
                        return new SampleValidationResponse(
                                header.getSampleCollectionHeaderId(),
                                patient.getId(),
                                fullName,
                                patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null,
                                patient.getPatientAge(),
                                patient.getPatientMobileNumber(),
                                subCharge != null ? subCharge.getSubId() : null,
                                subCharge != null ? subCharge.getSubName() : null,
                                patient.getUhidNo(),
                                header.getLastChgDate() != null ? header.getLastChgDate().toLocalDate() : null,
                                header.getCollection_time(),
                                header.getCollection_by(),
                                patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null,
                                investigations
                        );
                    })
                    .toList();

            log.info("Investigation status process ended..");
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Investigation status Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public ApiResponse<List<ResultResponse>> getValidatedResultEntries() {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            // Fetch details using your query
            List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllByHeaderResultEntryAndValidationStatusLogic();

            // 🟢 Group by Sample Collection Header (not patient + subChargeCode)
            Map<String, ResultResponse> responseMap = new LinkedHashMap<>();

            for (DgSampleCollectionDetails detail : detailsList) {

                DgSampleCollectionHeader header = detail.getSampleCollectionHeader();
                Long headerId = header.getSampleCollectionHeaderId();
                String key = String.valueOf(headerId); // Grouping by header ID

                // Group by Header
                ResultResponse response = responseMap.computeIfAbsent(
                        key,
                        k -> {
                            ResultResponse r = new ResultResponse();
                            var patient = header.getPatientId();

                            String fullName = Stream.of(
                                            patient.getPatientFn(),
                                            patient.getPatientMn(),
                                            patient.getPatientLn()
                                    )
                                    .filter(Objects::nonNull)
                                    .filter(s -> !s.isBlank())
                                    .collect(Collectors.joining(" "));

                            r.setPatientId(patient.getId());
                            r.setPatientName(fullName);
                            r.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null);
                            r.setRelationId(patient.getPatientRelation() != null ? patient.getPatientRelation().getId() : null);
                            r.setPatientGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null);
                            r.setPatientAge(patient.getPatientAge());
                            r.setPatientPhoneNo(patient.getPatientMobileNumber());

                            DgOrderHd dgOrderHd = labHdRepository.findByVisitId(header.getVisitId());
                            r.setOrderDate(String.valueOf(dgOrderHd.getOrderDate()));
                            r.setEnteredBy(currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName());
                            r.setCollectedDate(header.getCollection_time());
                            r.setCollectedTime(header.getCollection_time() != null ? header.getCollection_time().toLocalTime() : null);
                            r.setOrderNo(patient.getUhidNo());
                            r.setDepartment(header.getDepartmentId() != null ? header.getDepartmentId().getDepartmentName() : null);

                            MasSubChargeCode masSubChargeCode =
                                    subChargeCodeRepository.findById(header.getSubChargeCode().getSubId()).orElseThrow();
                            r.setMainChargeCodeId(masSubChargeCode.getMainChargeId().getChargecodeId());
                            r.setDoctorName(header.getHospitalId() != null ? header.getHospitalId().getHospitalName() : null);
                            r.setVisitId(header.getVisitId() != null ? header.getVisitId().getId() : null);
                            r.setSampleCollectionHeaderId(headerId);
                            r.setSubChargeCodeId(header.getSubChargeCode().getSubId());
                            r.setSubChargeCodeName(header.getSubChargeCode().getSubName());
                            r.setResultInvestigationResponseList(new ArrayList<>());
                            return r;
                        }
                );

                // 🧩 Group by Investigation
                ResultInvestigationResponse investigation = response.getResultInvestigationResponseList().stream()
                        .filter(i -> i.getInvestigationId().equals(detail.getInvestigationId().getInvestigationId()))
                        .findFirst()
                        .orElseGet(() -> {
                            DgMasInvestigation invObj = detail.getInvestigationId();

                            ResultInvestigationResponse inv = new ResultInvestigationResponse();
                            inv.setInvestigationId(invObj.getInvestigationId());
                            inv.setInvestigationName(invObj.getInvestigationName());
                            inv.setSampleCollectionDetailsId(detail.getSampleCollectionDetailsId());
                            inv.setResultType(invObj.getInvestigationType());

                            // --- Sample details
                            if (invObj.getSampleId() != null) {
                                inv.setSampleId(invObj.getSampleId().getId());
                                inv.setSampleName(invObj.getSampleId().getSampleDescription());
                            }

                            // --- Unit details
                            if (invObj.getUomId() != null) {
                                inv.setUnitId(invObj.getUomId().getId());
                                inv.setUnitName(invObj.getUomId().getName());
                            }

                            String normalRange = null;
                            if (invObj.getNormalValue() != null && !invObj.getNormalValue().isBlank()) {
                                normalRange = invObj.getNormalValue();
                            } else if (invObj.getMinNormalValue() != null && invObj.getMaxNormalValue() != null) {
                                normalRange = invObj.getMinNormalValue() + " - " + invObj.getMaxNormalValue();
                            }

                            inv.setNormalValue(normalRange);

                            inv.setResultSubInvestigationResponseList(new ArrayList<>());
                            response.getResultInvestigationResponseList().add(inv);
                            return inv;
                        });

                // 🧪 Fetch Sub-Investigations
                List<DgSubMasInvestigation> subList =
                        dgSubMasInvestigationRepository.findByInvestigationId(detail.getInvestigationId().getInvestigationId());

                for (DgSubMasInvestigation subInvest : subList) {
                    ResultSubInvestigationResponse sub = new ResultSubInvestigationResponse();
                    sub.setSubInvestigationId(subInvest.getSubInvestigationId());
                    sub.setSubInvestigationName(subInvest.getSubInvestigationName());
                    sub.setSampleId(subInvest.getSampleId() != null ? subInvest.getSampleId().getId() : null);
                    sub.setSampleName(subInvest.getSampleId() != null ? subInvest.getSampleId().getSampleDescription() : null);
                    sub.setUnit(subInvest.getUomId() != null ? subInvest.getUomId().getName() : null);
                    sub.setComparisonType(subInvest.getComparisonType());
                    sub.setResultType(subInvest.getResultType());

                    // --- Patient info for Normal Range
                    var patient = header.getPatientId();
                    String gender = patient.getPatientGender() != null ? patient.getPatientGender().getGenderCode() : null;
                    String ageStr = patient.getPatientAge(); // e.g. "24Y 8M 9D"

                    Long ageInYears = null;
                    if (ageStr != null && ageStr.matches("\\d+Y.*")) {
                        try {
                            ageInYears = Long.parseLong(ageStr.substring(0, ageStr.indexOf("Y")).trim());
                        } catch (Exception ignored) {}
                    }

                    // --- Fetch Normal Value
                    DgNormalValue dgNormalValue = null;
                    if (ageInYears != null && gender != null) {
                        dgNormalValue = dgNormalValueRepository
                                .findFirstBySubInvestigationIdAndSexAndFromAgeLessThanEqualAndToAgeGreaterThanEqual(
                                        subInvest, gender.substring(0, 1).toUpperCase(), ageInYears, ageInYears);
                    } else {
                        dgNormalValue = dgNormalValueRepository.findBySubInvestigationId(subInvest);
                    }

                    if (dgNormalValue != null) {
                        String normalRange = null;
                        if (dgNormalValue.getNormalValue() != null && !dgNormalValue.getNormalValue().isBlank()) {
                            normalRange = dgNormalValue.getNormalValue();
                        } else if (dgNormalValue.getMinNormalValue() != null && dgNormalValue.getMaxNormalValue() != null) {
                            normalRange = dgNormalValue.getMinNormalValue() + " - " + dgNormalValue.getMaxNormalValue();
                        }

                        sub.setNormalValue(normalRange);
                        sub.setNormalId(dgNormalValue.getNormalId());
                    }

                    // --- Fetch Fixed Values
                    List<DgFixedValue> dgFixedValue = dgFixedValueRepository.findBySubInvestigationId(subInvest);
                    List<DgFixedValueResponse> dgFixedValueResponses = new ArrayList<>();
                    for (DgFixedValue dgFixedValue1 : dgFixedValue) {
                        DgFixedValueResponse dgFixedValueResponse = new DgFixedValueResponse();
                        dgFixedValueResponse.setFixedId(dgFixedValue1.getFixedId());
                        dgFixedValueResponse.setFixedValue(dgFixedValue1.getFixedValue());
                        dgFixedValueResponse.setSubInvestigationId(subInvest.getSubInvestigationId());
                        dgFixedValueResponses.add(dgFixedValueResponse);
                    }
                    sub.setDgFixedValueResponseList(dgFixedValueResponses);

                    investigation.getResultSubInvestigationResponseList().add(sub);
                }
            }

            //  Return success response
            return ResponseUtils.createSuccessResponse(new ArrayList<>(responseMap.values()), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Investigation status Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.BAD_REQUEST.value());
        }
    }

}



