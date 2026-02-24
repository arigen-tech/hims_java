package com.hims.controller;

import com.hims.request.RadRegInvReq;
import com.hims.request.RadiologyReportRequest;
import com.hims.response.*;
import com.hims.service.RadiologyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        return radiologyService.getPendingRadiology(modality, patientName, phoneNumber, page, size);
    }
    //  status use cancel=c and complete=y
    @PutMapping("/cancelOrCompleteInvestigationRadiology")
    public ApiResponse<String> cancelOrCompleteInvestigationRadiology(@RequestParam Long id,@RequestParam String status) {
        return radiologyService.cancelOrCompleteInvestigationRadiology(id,status);
    }
    @GetMapping("/pendingListForRadiologyReport")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingListForRadiologyReport(
            @RequestParam(required = false) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return radiologyService.getPendingListForRadiologyReport(modality, patientName, phoneNumber, page, size);
    }
    @PostMapping("/saveDetailsReportForRadiology")
    public ResponseEntity<ApiResponse<String>> saveDetailsReportForRadiology(
            @Valid @RequestBody RadiologyReportRequest request,
            @RequestParam String status) {
        ApiResponse<String> response = radiologyService.saveDetailsReportForRadiology(request, status);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    @GetMapping("/pendingListForRadiologyReport")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingReportRadiology(
            @RequestParam(required = false) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return radiologyService.getPendingReportRadiology(modality, patientName, phoneNumber, page, size);
    }
    @PostMapping("/saveDetailsReportForRadiology")
    public ResponseEntity<ApiResponse<String>> addDetailsReport(
            @Valid @RequestBody RadiologyReportRequest request,
            @RequestParam String status) {
        ApiResponse<String> response = radiologyService.add(request, status);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getPACSStudyList")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPACSStudyList(
            @RequestParam(required = false) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return radiologyService.getPACSStudyList(modality, patientName, phoneNumber, page, size);
    }


}


