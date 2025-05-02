package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.response.DgUomResponse;
import com.hims.service.DgMasInvestigationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class DgMasInvestigationServiceImp implements DgMasInvestigationService {
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;


    @Override
    public ApiResponse<List<DgMasInvestigationResponse>> getInvestigationWithPriceDetails(String investigationName, String genderApplicable) {
        LocalDate currentDate = LocalDate.now();
        List<DgMasInvestigationResponse> responseList =dgMasInvestigationRepository.findByPriceDetails( investigationName,  genderApplicable,currentDate );
       // System.out.println(responseList);
        if (responseList.isEmpty()) {
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "data not found", 404);
        } else {
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        }

    }
}
