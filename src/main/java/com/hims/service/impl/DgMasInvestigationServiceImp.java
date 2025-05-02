package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.service.DgMasInvestigationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
