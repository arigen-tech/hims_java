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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SampleValidationServiceImpl implements SampleValidationService {
    @Autowired
     DgSampleCollectionDetailsRepository detailsRepo;
    @Autowired
    DgSampleCollectionHeaderRepository headerRepo;



    @Override
    @Transactional
    public void validateInvestigations(List<InvestigationValidationRequest> requests) {
//        for (InvestigationValidationRequest req : requests) {
//            String validated = req.getAccepted() != null && req.getAccepted() ? "y" : "n";
//           // String reason = req.getRejected() != null && req.getRejected() ? req.getReason() : null;
//            detailsRepo.updateValidationStatus(req.getDetailId(), validated);
//        }
        // 1. Update each investigation’s validated flag
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
    }

    @Override
    public ApiResponse<List<SampleValidationResponse>> getInvestigationsWithOrderStatusNAndP() {
        List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllByHeaderOrderStatusNOrP();
        if (detailsList.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "No investigations found with order status n or p", 404);
        }

        // Group by patient
        Map<Long, List<DgSampleCollectionDetails>> groupedByPatient = detailsList.stream()
                .collect(Collectors.groupingBy(d -> d.getSampleCollectionHeader().getPatientId().getId()));

        List<SampleValidationResponse> responseList = new ArrayList<>();

        for (Map.Entry<Long, List<DgSampleCollectionDetails>> entry : groupedByPatient.entrySet()) {
            DgSampleCollectionDetails first = entry.getValue().get(0);
            DgSampleCollectionHeader header = first.getSampleCollectionHeader();
            var patient = header.getPatientId();

            List<TestDetailsDTO> tests = entry.getValue().stream().map(d ->
                    new TestDetailsDTO(
                            d.getSampleCollectionDetailsId(),
                            d.getInvestigationId()!=null? d.getInvestigationId().getSampleId().getSampleCode():null,
                            d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                            d.getInvestigationId()!=null? d.getInvestigationId().getSampleId().getId():null,
                            d.getSampleId()!=null?d.getSampleId().getSampleDescription():null,
                            d.getQuantity(),
                            d.getEmpanelledStatus(),
                            d.getSampleCollDatetime(),
                            d.getRejected_reason(),
                            d.getRemarks()
                    )
            ).toList();
            responseList.add(new SampleValidationResponse(
                    patient.getId(),
                    patient.getPatientFn(),
                    patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null,
                    patient.getEmerMobile(),
                    header.getSubChargeCode().getMainChargeId().getChargecodeName(),
                    patient.getUhidNo(),
                    header.getLastChgDate() != null ? header.getLastChgDate().toLocalDate() : null,
                    header.getCollection_time(),
                    header.getCollection_by(),
                    patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null,
                    tests
            ));
        }
        return ResponseUtils.createSuccessResponse( responseList, new TypeReference<>() {});
    }
}

