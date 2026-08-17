package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.request.LabRadioRegistrationRequest;
import com.hims.request.LabRadioUpdateRequest;
import com.hims.request.RadRegInvReq;
import com.hims.request.RadiologyReportRequest;
import com.hims.response.*;
import com.hims.service.RadiologyService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
    @RequestMapping("/radiology")
@Slf4j
public class RadiologyController {

    @Autowired
    RadiologyService radiologyService;

    @PostMapping("/registerWithInv")
    public ResponseEntity<ApiResponse<LabRadiologyRegistrationResponse>> registerPatient(@RequestBody RadRegInvReq request) {
        ApiResponse<LabRadiologyRegistrationResponse> response = radiologyService.registerPatientWithInv(request.getPatient(), request.getRadInvestigationReq());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/radiologyRegistration")
    public ResponseEntity<ApiResponse<LabRadiologyRegistrationResponse>> registerAndBookingRadiology(
            @Valid @RequestBody LabRadioRegistrationRequest request) {

        log.info("Radiology Registration API called for patient: {} {}",
                request.getPatient().getPatientFn(), request.getPatient().getPatientLn());

        ApiResponse<LabRadiologyRegistrationResponse> response =
                radiologyService.registerAndBookingRadiology(request.getPatient(), request.getInvestigationReq());

        HttpStatus status = HttpStatus.resolve(response.getStatus());
        return ResponseEntity.status(status != null ? status : HttpStatus.OK).body(response);
    }

    @PutMapping("/updateDetailsAndBookingRadiology")
    public ResponseEntity<LabRadioUpdateResponse> updateDetailsAndBooking(
            @Valid @RequestBody LabRadioUpdateRequest request) {

        log.info("Updating details and booking radiology for patient: {} {}",
                request.getPatient().getPatientFn(), request.getPatient().getPatientLn());

        LabRadioUpdateResponse response = radiologyService.updatePatientDetailsAndBooking(request);
        log.info("Operation successful for patient. Billings generated: {}",
                (response.getBillingHeaderIds() != null ? response.getBillingHeaderIds().size() : 0));

        return ResponseEntity.ok(response);
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

    @GetMapping("/getDetailsReportForRadiology")
    public ResponseEntity<ApiResponse<RadiologyReportResponse>> getDetailsReportForRadiology(
            @RequestParam Long radOrderDtId) {
        ApiResponse<RadiologyReportResponse> response = radiologyService.getDetailsReportForRadiology(radOrderDtId);
        return ResponseEntity.ok(response);
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

    @GetMapping("/orderTrackingByInpatientIdOrAccesionNo")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> orderTrackingByInpatientIdOrAccesionNo(
            @RequestParam(required = false) Long inpatientId,
            @RequestParam(required = false) String accesionNo) {
        return ResponseEntity.ok(radiologyService.orderTrackingByInpatientIdOrAccesionNo(inpatientId, accesionNo));
    }

}

