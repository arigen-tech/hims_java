package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgSampleCollectionDetails;
import com.hims.entity.DgSampleCollectionHeader;
import com.hims.entity.repository.DgSampleCollectionDetailsRepository;
import com.hims.entity.repository.DgSampleCollectionHeaderRepository;
import com.hims.request.InvestigationValidationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.SampleValidationResponse;
import com.hims.response.TestDetailsDTO;
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
            log.info("Investigation status Process Started..");
            // Step 1: Fetch filtered details based on header/detail validated status
            List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllByHeaderValidatedStatusLogic();


            // Step 2: Group by patient
            Map<Long, List<DgSampleCollectionDetails>> groupedByPatient = detailsList.stream()
                    .collect(Collectors.groupingBy(d -> d.getSampleCollectionHeader().getPatientId().getId()));

            // Step 3: Map grouped data to response DTOs
            List<SampleValidationResponse> responseList = groupedByPatient.entrySet().stream()
                    .map(entry -> {
                        List<DgSampleCollectionDetails> patientDetails = entry.getValue();
                        DgSampleCollectionHeader header = patientDetails.get(0).getSampleCollectionHeader();
                        var patient = header.getPatientId();
                        String fullName = Stream.of(patient.getPatientFn(), patient.getPatientMn(), patient.getPatientLn())
                                .filter(Objects::nonNull)
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.joining(" "));

                        // Map TestDetailsDTO using only DgSampleCollectionDetails fields
                        List<TestDetailsDTO> investigations = patientDetails.stream()
                                .map(d -> new TestDetailsDTO(
                                        d.getSampleCollectionDetailsId(),
                                        d.getInvestigationId() != null ? d.getInvestigationId().getSampleId().getSampleCode() : null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getSampleId().getId() : null,
                                        d.getSampleId() != null ? d.getSampleId().getSampleDescription() : null,
                                        d.getQuantity(),
                                        d.getEmpanelledStatus(),
                                        d.getSampleCollDatetime(),
                                        d.getRejected_reason(),
                                        d.getRemarks()
                                ))
                                .toList();

                        // Build patient-level response
                        return new SampleValidationResponse(
                                patient.getId(),
                                fullName,
                                patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null,
                                patient.getPatientMobileNumber(),
                                header.getSubChargeCode().getMainChargeId().getChargecodeCode(),
                                patient.getUhidNo(),
                                header.getLastChgDate() != null ? header.getLastChgDate().toLocalDate() : null,
                                header.getCollection_time(),
                                header.getCollection_by(),
                                patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null,
                                investigations
                        );
                    })
                    .toList();
            log.info("Investigation status Process Ended..");
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });
        }catch (Exception e) {
            log.error("Investigation status  Error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.BAD_REQUEST.value());
        }
    }
}



