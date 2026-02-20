package com.hims.controller;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.OPDService;
import com.hims.utils.StockFound;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/opd")
@RequiredArgsConstructor
@Slf4j
public class OPDController {

    private final OPDService opdService;
    private final StockFound stockFound;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get patient vitals by visit ID
     */
    @GetMapping("/patientVitals")
    public ResponseEntity<ApiResponse<OpdPatientVitalResponce>> getPatientVitals(
            @RequestParam @Parameter(description = "Visit ID") Long visitId) {
        log.info("GET /opd/patientVitals called: visitId={}", visitId);
        ApiResponse<OpdPatientVitalResponce> response = opdService.getPatientVitals(visitId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create OPD patient detail
     */
    @PostMapping("/createOPDPatientDetail")
    public ResponseEntity<ApiResponse<OpdPatientDetail>> createOPDPatientDetail(
            @Valid @RequestBody OpdPatientDetailFinalRequest request) {
        log.info("POST /opd/createOPDPatientDetail called");
        ApiResponse<OpdPatientDetail> response = opdService.createOPDPatientDetail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update recall patient
     */
    @PutMapping("/updateRecallPatient")
    public ResponseEntity<ApiResponse<OpdPatientDetail>> updateRecallPatient(
            @Valid @RequestBody RecallOpdPatientDetailRequest request) {
        log.info("PUT /opd/updateRecallPatient called");
        ApiResponse<OpdPatientDetail> response = opdService.updateRecallPatient(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search active visits
     */
    @PostMapping("/searchActiveVisits")
    public ResponseEntity<ApiResponse<List<OpdPatientDetailsWaitingresponce>>> searchActiveVisits(
            @RequestBody ActiveVisitSearchRequest request) {
        log.info("POST /opd/searchActiveVisits called");
        return ResponseEntity.ok(opdService.searchActiveVisits(request));
    }

    /**
     * Get recall OPD visits
     */
    @GetMapping("/recallOPDVisits")
    public ResponseEntity<ApiResponse<List<OpdPatientRecallResponce>>> getRecallOPDVisits(
            @RequestParam(required = false) @Parameter(description = "Patient name") String name,
            @RequestParam(required = false) @Parameter(description = "Mobile number") String mobile,
            @RequestParam(required = false) @Parameter(description = "Visit date in yyyy-MM-dd format")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        log.info("GET /opd/recallOPDVisits called: name={}, mobile={}, visitDate={}", name, mobile, visitDate);
        ApiResponse<List<OpdPatientRecallResponce>> response = opdService.getRecallOPDVisits(name, mobile, visitDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Close visit
     */
    @PutMapping("/closeVisit/{visitId}/{status}")
    public ApiResponse<String> closeVisit(
            @PathVariable @Parameter(description = "Visit ID") Long visitId,
            @PathVariable @Parameter(description = "Status") String status) {
        log.info("PUT /opd/closeVisit/{}/{} called", visitId, status);
        return opdService.closeVisit(visitId, status);
    }

    /**
     * Update visit status
     */
    @PutMapping("/updateVisitStatus")
    public ResponseEntity<Visit> updateVisitStatus(
            @RequestParam @Parameter(description = "Visit ID") Long visitId,
            @RequestParam @Parameter(description = "Visit date") Instant visitDate,
            @RequestParam @Parameter(description = "Doctor ID") Long doctorId) {
        log.info("PUT /opd/updateVisitStatus called: visitId={}, doctorId={}", visitId, doctorId);
        Visit updatedVisit = opdService.updateVisitStatus(visitId, visitDate, doctorId);
        messagingTemplate.convertAndSend("/topic/statusUpdated", "updated");
        return ResponseEntity.ok(updatedVisit);
    }

    /**
     * Get pending pre-consultations
     */
    @GetMapping("/pendingPreConsultations")
    public ResponseEntity<ApiResponse<List<Visit>>> getPendingPreConsultations() {
        log.info("GET /opd/pendingPreConsultations called");
        ApiResponse<List<Visit>> response = opdService.getPendingPreConsultations();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Save vitals
     */
    @PostMapping("/saveVitals")
    public ResponseEntity<ApiResponse<String>> saveVitals(
            @RequestBody OpdPatientDetailRequest request) {
        log.info("POST /opd/saveVitals called");
        ApiResponse<String> response = opdService.saveVitals(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get OPD waiting list
     */
    @GetMapping("/getOPDWaitingList")
    public ResponseEntity<ApiResponse<List<Visit>>> getOPDWaitingList() {
        log.info("GET /opd/getOPDWaitingList called");
        ApiResponse<List<Visit>> response = opdService.getOPDWaitingList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get patient visit history
     */
    @GetMapping("/patientVisitHistory/{patientId}")
    public ApiResponse<List<VisitResponse>> getPatientVisitHistory(
            @PathVariable @Parameter(description = "Patient ID") int patientId) {
        log.info("GET /opd/patientVisitHistory/{} called", patientId);
        return opdService.getPatientVisitHistory(patientId);
    }

    /**
     * Get available stock
     */
    @GetMapping("/getAvailableStock")
    public ResponseEntity<Long> getAvailableStock(
            @RequestParam @Parameter(description = "Hospital ID") Long hospitalId,
            @RequestParam @Parameter(description = "Department ID") Integer departmentId,
            @RequestParam @Parameter(description = "Item ID") Long itemId,
            @RequestParam @Parameter(description = "Number of days") Integer days) {
        log.info("GET /opd/getAvailableStock called");
        Long availableStock = opdService.getAvailableStock(hospitalId, departmentId, itemId, days);
        if (availableStock == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(availableStock);
    }

    /**
     * Get template by ID
     */
    @GetMapping("/template/{templateId}")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> getTemplate(
            @PathVariable @Parameter(description = "Template ID") Long templateId) {
        log.info("GET /opd/template/{} called", templateId);
        return new ResponseEntity<>(opdService.getTemplate(templateId), HttpStatus.OK);
    }

    /**
     * Get templates by type
     */
    @GetMapping("/templates/type/{templateType}")
    public ResponseEntity<ApiResponse<List<OpdTemplateResponse>>> getTemplatesByType(
            @PathVariable @Parameter(description = "Template type") int templateType) {
        log.info("GET /opd/templates/type/{} called", templateType);
        return new ResponseEntity<>(opdService.getTemplatesByType(templateType), HttpStatus.OK);
    }

    /**
     * Create OPD template
     */
    @PostMapping("/createOPDTemplate")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> createOPDTemplate(
            @RequestBody OpdTemplateRequest request) {
        log.info("POST /opd/createOPDTemplate called");
        return new ResponseEntity<>(opdService.createOPDTemplate(request), HttpStatus.CREATED);
    }

    /**
     * Update OPD template
     */
    @PutMapping("/updateOPDTemplate")
    public ResponseEntity<ApiResponse<String>> updateOPDTemplate(
            @RequestBody OpdTempInvReq request) {
        log.info("PUT /opd/updateOPDTemplate called");
        return new ResponseEntity<>(opdService.updateOPDTemplate(request), HttpStatus.OK);
    }

    /**
     * Add investigations to template
     */
    @PutMapping("/addTemplateInvestigations")
    public ResponseEntity<ApiResponse<InvestigationByTemplateResponse>> addTemplateInvestigations(
            @RequestBody InvestigationByTemplateRequest request) {
        log.info("PUT /opd/addTemplateInvestigations called");
        return new ResponseEntity<>(opdService.addTemplateInvestigations(request), HttpStatus.OK);
    }

    /**
     * Save template treatment
     */
    @PostMapping("/saveTemplateTreatment")
    public ApiResponse<OpdTemplateResponse> saveTemplateTreatment(
            @RequestBody OpdTemplateRequest request) {
        log.info("POST /opd/saveTemplateTreatment called");
        return opdService.saveTemplateTreatment(request);
    }

    /**
     * Update template treatment
     */
    @PutMapping("/updateTemplateTreatment/{templateId}")
    public ApiResponse<OpdTemplateResponse> updateTemplateTreatment(
            @PathVariable @Parameter(description = "Template ID") Long templateId,
            @RequestBody OpdTemplateRequest request) {
        log.info("PUT /opd/updateTemplateTreatment/{} called", templateId);
        return opdService.updateTemplateTreatment(templateId, request);
    }

    /**
     * Get all template treatments by type
     */
    @GetMapping("/getAllTemplateTreatments/type/{templateType}")
    public ApiResponse<List<OpdTemplateResponse>> getAllTemplateTreatments(
            @PathVariable @Parameter(description = "Template type") int templateType) {
        log.info("GET /opd/getAllTemplateTreatments/type/{} called", templateType);
        return opdService.getAllTemplateTreatments(templateType);
    }
}



