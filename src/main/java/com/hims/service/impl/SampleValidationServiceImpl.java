package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgSampleCollectionDetails;
import com.hims.entity.DgSampleCollectionHeader;
import com.hims.entity.DgSubMasInvestigation;
import com.hims.entity.repository.DgSampleCollectionDetailsRepository;
import com.hims.entity.repository.DgSampleCollectionHeaderRepository;
import com.hims.entity.repository.DgSubMasInvestigationRepository;
import com.hims.request.InvestigationValidationRequest;
import com.hims.response.*;
import com.hims.service.SampleValidationService;
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
                } else if (accepted > 0) {
                    orderStatus = "p"; // partial
                } else {
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
    public ApiResponse<List<ValidatedResponse>> getValidatedResultEntries() {
        try {
// 1️⃣ Fetch all validated details
            List<DgSampleCollectionDetails> detailsList = detailsRepo.findValidatedDetailsForResultEntry();

            Map<Long, ValidatedResponse> responseMap = new LinkedHashMap<>();

            for (DgSampleCollectionDetails detail : detailsList) {

                DgSampleCollectionHeader header = detail.getSampleCollectionHeader();

                // --- Group by Patient / Header ---
                ValidatedResponse response = responseMap.computeIfAbsent(
                        header.getPatientId().getId(),
                        k -> {
                            ValidatedResponse r = new ValidatedResponse();
                            r.setPatientId(header.getPatientId().getId());
                            r.setPatientName(header.getPatientId().getPatientFn());
                            r.setRelation(header.getPatientId() != null ? header.getPatientId().getPatientRelation().getRelationName() : null);
                            r.setPatientGender(header.getPatientId().getPatientGender() != null ? header.getPatientId().getPatientGender().getGenderName() : null);
                            r.setPatientAge(header.getPatientId().getPatientAge() != null ? header.getPatientId().getPatientAge() + " Years" : null);
                            r.setCollectedDate(header.getCollection_time());
                            r.setCollectedTime(header.getCollection_time() != null ? header.getCollection_time().toLocalTime() : null);
                            r.setOrderNo(header.getPatientId() != null ? header.getPatientId().getUhidNo() : null);
                            r.setDepartment(header.getDepartmentId() != null ? header.getDepartmentId().getDepartmentName() : null);
                            r.setDoctorName(header.getHospitalId() != null ? header.getHospitalId().getHospitalName() : null);
                            r.setResultInvestigationResponseList(new ArrayList<>());
                            return r;
                        }
                );

                // --- Group by Investigation ---
                ResultInvestigationResponse investigation = response.getResultInvestigationResponseList().stream()
                        .filter(i -> i.getInvestigationId().equals(detail.getInvestigationId().getInvestigationId()))
                        .findFirst()
                        .orElseGet(() -> {
                            ResultInvestigationResponse inv = new ResultInvestigationResponse();
                            inv.setInvestigationId(detail.getInvestigationId().getInvestigationId());
                            inv.setInvestigationName(detail.getInvestigationId().getInvestigationName());
                            inv.setResultSubInvestigationResponseList(new ArrayList<>());
                            response.getResultInvestigationResponseList().add(inv);
                            return inv;
                        });

                // --- Fetch all Sub-Investigations for this Investigation from DB ---
                List<DgSubMasInvestigation> subList = dgSubMasInvestigationRepository
                        .findByInvestigationId(detail.getInvestigationId().getInvestigationId());

                // --- Add Sub-Investigations ---
                for (DgSubMasInvestigation subInvest : subList) {
                    ResultSubInvestigationResponse sub = new ResultSubInvestigationResponse();
                    sub.setSubInvestigationId(subInvest.getSubInvestigationId());
                    sub.setSubInvestigationName(subInvest.getSubInvestigationName());
                    sub.setSampleId(subInvest.getSampleId() != null ? subInvest.getSampleId().getId() : null);
                    sub.setSampleName(subInvest.getSampleId() != null ? subInvest.getSampleId().getSampleDescription() : null);
                    sub.setUnit(subInvest.getUomId() != null ? subInvest.getUomId().getName() : null);
                    investigation.getResultSubInvestigationResponseList().add(sub);
                }
            }
            ;

            return ResponseUtils.createSuccessResponse(new ArrayList<>(responseMap.values()), new TypeReference<>() {
            });
        }catch (Exception e) {
            log.error("Investigation status  Error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.BAD_REQUEST.value());
    }
}}



