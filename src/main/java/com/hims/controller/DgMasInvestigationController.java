package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.response.MasBloodGroupResponse;
import com.hims.service.DgMasInvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/DgMasInvestigation")
public class DgMasInvestigationController {
    @Autowired
    private DgMasInvestigationService dgMasInvestigationService;


    @GetMapping("/price-details")
    public ApiResponse<List<DgMasInvestigationResponse>> getInvestigationPriceDetails(
            @RequestParam String genderApplicable,
            @RequestParam String investigationName
    ) {return dgMasInvestigationService.getPriceDetails(genderApplicable, investigationName);
    }

    @GetMapping("/getAll/{flag}")
    public ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(@PathVariable int flag) {
        return dgMasInvestigationService.getAllInvestigations(flag);
    }

}
