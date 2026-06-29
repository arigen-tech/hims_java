package com.hims.controller;

import com.hims.request.DgMasInvestigationMultiRequest;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgMasInvestigationSingleReqest;
import com.hims.response.*;
import com.hims.service.DgMasInvestigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/DgMasInvestigation")
@Slf4j
public class DgMasInvestigationController {

    @Autowired
    private DgMasInvestigationService dgMasInvestigationService;

    @GetMapping("/price-details")
    public ApiResponse<List<DgMasInvestigationPriceDetailsResponse>> getInvestigationPriceDetails(
            @RequestParam String genderApplicable,@RequestParam(required = false) Boolean radioFlag
           // @RequestParam String investigationName
    ) {
        if (radioFlag == null)
            radioFlag=false;
        return dgMasInvestigationService.getPriceDetails(genderApplicable,radioFlag);
    }
    @GetMapping("/getAll/{flag}")
    public ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(
            @PathVariable int flag,
            @RequestParam(required = false) Long mainChargeCodeId) {

        return dgMasInvestigationService.getAllInvestigations(flag, mainChargeCodeId);
    }
    @GetMapping("/dynamic/all")
    public ApiResponse<Page<DgMasInvestigationResponse>> getAllInvestigationsDynamic(
            @RequestParam int flag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long mainChargeCodeId
    ) {
        return dgMasInvestigationService
                .getAllInvestigationsDynamic(flag, page, size, search, mainChargeCodeId);
    }
    @GetMapping("/dgMasInvestigationByMainChargeCodeId")
    public ApiResponse<List<MasInvestigationByMainChargeCodeResponse>> dgMasInvestigationByMainChargeCodeId(
            @RequestParam Long mainChargeCodeId
    ) {
        return dgMasInvestigationService.dgMasInvestigationByMainChargeCodeId( mainChargeCodeId);
    }



    @GetMapping("/investigationCategoryTypes")
    public ResponseEntity<?> getInvestigationTypes() {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("response", dgMasInvestigationService.getInvestigationTypes());

        return ResponseEntity.ok(response);
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

    @GetMapping("/mas-investigation/all")
    public  ResponseEntity<?> getAll(){
        return  ResponseEntity.ok(dgMasInvestigationService.getAllInvestigations());
    }

    @GetMapping("/getDgMasInvestigationBySearch")
    public ApiResponse<Page<InvestigationResponse>> getDgMasInvestigation(
            @RequestParam Long investigationId,
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Request received for getDgMasInvestigation | investigationId: {}, search: {}, page: {}, size: {}",
                investigationId, search, page, size);

        return dgMasInvestigationService.getDgMasInvestigation(investigationId, search, page, size);
    }

}
