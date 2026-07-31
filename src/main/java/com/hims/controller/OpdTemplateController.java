package com.hims.controller;

import com.hims.request.InvestigationByTemplateRequest;
import com.hims.request.OpdTempInvReq;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InvestigationByTemplateResponse;
import com.hims.response.OpdTemplateResponse;
import com.hims.service.OpdTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "OpdTemplate")
@RequestMapping("/opdTemplate")
@Slf4j
public class OpdTemplateController {

    @Autowired
    private OpdTemplateService opdTempService;

    @GetMapping("/getById/{templateId}")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> getByTemplate (@PathVariable Long templateId){
        return new ResponseEntity<>(opdTempService.getByTemplateId(templateId), HttpStatus.OK);
    }

    /**
     * Retrieves all OPD template investigations filtered by status flag.
     *
     * This endpoint returns a list of OPD templates containing investigations based on the provided flag.
     * Flag values: 0 = All templates, 1 = Active templates only
     *
     * @param flag status filter flag (0 for all templates, 1 for active templates only)
     * @return ResponseEntity containing ApiResponse with list of OpdTemplateResponse containing investigation details
     */
    @GetMapping("/getInvestigationsTemplates/{flag}")
    @Operation(
            summary = "Get All OPD Template Investigations by Status",
            description = "Retrieves OPD investigation templates filtered by status and optionally by doctor."
    )
    public ResponseEntity<ApiResponse<List<OpdTemplateResponse>>> getTemplateInvestigations(
            @PathVariable int flag,
            @RequestParam(required = false) Long doctorId) {

        log.info("Fetching OPD template investigations with flag: {} and doctorId: {}", flag, doctorId);

        return ResponseEntity.ok(opdTempService.getAllTemplateInvestigations(flag, doctorId));
    }

    @PostMapping("/saveInvestigationTemplate")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> creatingOpdTemplate (@RequestBody OpdTemplateRequest opdTempReq){
        return new ResponseEntity<>(opdTempService.saveInvestigationTemplate(opdTempReq), HttpStatus.CREATED);
    }


    @PutMapping("/updateOpdTemplateInvestigation/{templateId}")
    public ResponseEntity<ApiResponse<String>> updatingOpdTemplate (
            @RequestBody OpdTempInvReq opdTempInvReq ) {
        return new ResponseEntity<>(opdTempService.updateOpdTemplate(opdTempInvReq), HttpStatus.OK);
    }

    @PutMapping("/add-multi-investigation/{templateId}")
    public ResponseEntity<ApiResponse<InvestigationByTemplateResponse>> addingMultiInvestigation(
            @RequestBody InvestigationByTemplateRequest investByTempReq ) {
        return new ResponseEntity<>(opdTempService.multiInvestigationTemplate(investByTempReq), HttpStatus.OK);
    }

    @PostMapping("/saveOpdTemplateTreatment")
    public ApiResponse<OpdTemplateResponse> saveOpdTemplateTreatment(@RequestBody OpdTemplateRequest request) {
        return opdTempService.saveOpdTemplateTreatment(request);
    }

    @PutMapping("/updateOpdTemplateTreatment/{templateId}")
    public ApiResponse<OpdTemplateResponse> update(@PathVariable Long templateId, @RequestBody OpdTemplateRequest request) {
        return opdTempService.updateOpdTemplateTreatment(templateId, request);
    }

    @GetMapping("/getAllTreatmentTemplate/{flag}")
    public ApiResponse<List<OpdTemplateResponse>> getAllOpdTemplateTreatments(
            @PathVariable int flag,
            @RequestParam(required = false) Long doctorId) {
        log.info("Fetching OPD treatment templates with flag: {} and doctorId: {}", flag, doctorId);
        return opdTempService.getAllOpdTemplateTreatments(flag, doctorId);
    }

}
