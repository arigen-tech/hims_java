package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.service.DgMasInvestigationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DgMasInvestigationServiceImp implements DgMasInvestigationService {
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;


    @Override
    public ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable, String investigationName) {
        List<Object[]> results = dgMasInvestigationRepository.findByPriceDetails(genderApplicable, investigationName);

        List<DgMasInvestigationResponse>  response = results.stream().map(obj -> {
            DgMasInvestigationResponse dto = new DgMasInvestigationResponse();
            dto.setInvestigationId(obj[0] != null ? ((Number) obj[0]).longValue() : null);
            dto.setInvestigationName((String) obj[1]);
            dto.setStatus(obj[2] != null ? obj[2].toString() : null);
            dto.setGenderApplicable(obj[3] != null ? obj[3].toString() : null);
            dto.setPrice(obj[4] != null ? ((Number) obj[4]).doubleValue() : 0.0);
            return dto;
        }).collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag) {
        try {
            List<DgMasInvestigation> investigationList;
            if (flag == 1) {
                investigationList = dgMasInvestigationRepository.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                investigationList = dgMasInvestigationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all, 1 for active.",
                        400
                );
            }

            if (investigationList == null) {
                investigationList = new ArrayList<>();
            }

            List<DgMasInvestigationResponse> responseList = investigationList.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error retrieving investigations: " + e.getMessage(),
                    500
            );
        }
    }

    private DgMasInvestigationResponse mapToResponse(DgMasInvestigation entity) {
        DgMasInvestigationResponse dto = new DgMasInvestigationResponse();
        dto.setInvestigationId(entity.getInvestigationId());
        dto.setInvestigationName(entity.getInvestigationName());
        dto.setStatus(entity.getStatus());
        dto.setInvestigationType(entity.getInvestigationType());
        dto.setPrice(entity.getPrice());
        dto.setGenderApplicable(entity.getGenderApplicable());

        return dto;
    }
}
