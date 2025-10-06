package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgSampleCollectionDetails;
import com.hims.entity.DgSampleCollectionHeader;
import com.hims.entity.repository.DgSampleCollectionDetailsRepository;
import com.hims.response.ApiResponse;
import com.hims.response.SampleValidationResponse;
import com.hims.response.TestDetailsDTO;
import com.hims.service.SampleValidationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SampleValidationServiceImpl implements SampleValidationService {
    @Autowired
     DgSampleCollectionDetailsRepository detailsRepo;

    @Override
    public ApiResponse<SampleValidationResponse> getPatientInvestigations(Long patientId) {
        List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllInvestigationsByPatient(patientId);
        if (detailsList.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Data not found", 404);
        }
        // Header(all details share same header for a patient)
        DgSampleCollectionHeader header = detailsList.get(0).getSampleCollectionHeaderId();
        List<TestDetailsDTO> investigations = new ArrayList<>();
        for (DgSampleCollectionDetails d : detailsList) {
            investigations.add(new TestDetailsDTO(
                    d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationId().toString() : null,
                    d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                    d.getSampleId() != null ? d.getSampleId().getSampleDescription() : null,
                    d.getQuantity(),
                    d.getEmpanelledStatus(),
                    d.getSampleCollDatetime(),

                    d.getRejected_reason(),
                    d.getRemarks()
            ));
        }
        return ResponseUtils.createSuccessResponse(new SampleValidationResponse(
                header.getPatient_id().getId(),
                header.getPatient_id().getPatientFn(),
                header.getPatient_id().getPatientGender().getGenderName(),
                header.getPatient_id().getEmerMobile(),
                header.getSubChargeCode().getMainChargeId().getChargecodeName(),
                header.getPatient_id().getUhidNo(),
                header.getLastChgDate() != null ? header.getLastChgDate().toLocalDate() : null,
                header.getCollection_time(),
                header.getCollection_by(),
                header.getPatient_id().getPatientRelation().getRelationName(),
                investigations
        ), new TypeReference<>() {
        });

    }
    }

