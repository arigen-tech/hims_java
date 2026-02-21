package com.hims.controller;

import com.hims.request.RadRegInvReq;
import com.hims.response.*;
import com.hims.service.RadiologyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
@RequestMapping("/radiology")
@Slf4j
public class RadiologyController {

    @Autowired
    RadiologyService radiologyService;
    @PostMapping("/registerWithInv")
    public ResponseEntity<ApiResponse<RadiologyAppSetupResponse>> registerPatient(@RequestBody RadRegInvReq request) {
        ApiResponse<RadiologyAppSetupResponse> response = radiologyService.registerPatientWithInv(request.getPatient(), request.getRadInvestigationReq());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/pendingInvestigationForRadiology")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingRadiology(
            @RequestParam(required = true) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return radiologyService.pendingRadiology(modality, patientName, phoneNumber, page, size);
    }
    //  status use cancel=c and complete=y
    @PutMapping("/cancelOrCompleteInvestigationRadiology")
    public ApiResponse<String> getInvestigationRadiology(@RequestParam Long id,@RequestParam String status) {
        return radiologyService.pendingInvestigationRadiology(id,status);
    }

}
