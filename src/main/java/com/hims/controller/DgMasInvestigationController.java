package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.response.MasBloodGroupResponse;
import com.hims.service.DgMasInvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/DgMasInvestigation")
public class DgMasInvestigationController {
    @Autowired
    private DgMasInvestigationService dgMasInvestigationService;


    @GetMapping("dgMasInvestigationById/{investigationName}/{genderApplicable}")
    public ResponseEntity<ApiResponse<List<DgMasInvestigationResponse>>> getInvestigationDetails(
            @PathVariable String investigationName,
            @PathVariable String genderApplicable) {


        return new ResponseEntity<>(dgMasInvestigationService.getInvestigationWithPriceDetails(investigationName, genderApplicable), HttpStatus.OK);

    }

    }
