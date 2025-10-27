package com.hims.controller;

import com.hims.request.DgMasInvestigationMultiRequest;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgMasInvestigationSingleReqest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.response.DgMasInvestigationSingleResponse;
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
            @RequestParam String genderApplicable
           // @RequestParam String investigationName
    ) {return dgMasInvestigationService.getPriceDetails(genderApplicable);
    }

    @GetMapping("/getAll/{flag}")
    public ApiResponse<List<DgMasInvestigationSingleResponse>> getAllInvestigations(@PathVariable int flag) {
        return dgMasInvestigationService.getAllInvestigations(flag);
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeInvestigationStatus(@PathVariable Long id,@RequestParam String status){
        return ResponseEntity.ok(dgMasInvestigationService.changeInvestigationStatus(id,status));
    }

    @PostMapping("/create-investigation")
    public ResponseEntity<ApiResponse<DgMasInvestigationSingleResponse>> addInvestigation(@RequestBody DgMasInvestigationSingleReqest investigationRequest){
        return new ResponseEntity<>(dgMasInvestigationService.createInvestigation(investigationRequest), HttpStatus.CREATED);
    }

    @PutMapping("/update-single-investigation/{investigationId}")
    public ResponseEntity<ApiResponse<DgMasInvestigationSingleResponse>> updateOneInvestigation(
            @PathVariable Long investigationId,
            @RequestBody DgMasInvestigationSingleReqest investigationRequest ) {
        return new ResponseEntity<>(dgMasInvestigationService.updateSingleInvestigation(investigationId, investigationRequest), HttpStatus.OK);
    }

    @PutMapping("/update-multiple-investigation/{investigationId}")
    public ResponseEntity<ApiResponse<String>> updateMultiInvestigation(
            @RequestBody DgMasInvestigationMultiRequest dmiMultiReq ) {
        return new ResponseEntity<>(dgMasInvestigationService.updateMultipleInvestigation(dmiMultiReq), HttpStatus.OK);
    }

}
