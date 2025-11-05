package com.hims.controller;

import com.hims.entity.OpdTemplateInvestigation;
import com.hims.request.InvestigationByTemplateRequest;
import com.hims.request.OpdTempInvReq;
import com.hims.request.OpdTemplateInvestigationRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InvestigationByTemplateResponse;
import com.hims.response.OpdTemplateInvestigationResponse;
import com.hims.response.OpdTemplateResponse;
import com.hims.service.OpdTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "OpdTemplate")
@RequestMapping("/opdTemplate")
public class OpdTemplateController {

    @Autowired
    private OpdTemplateService opdTempService;

    @GetMapping("/getById/{templateId}")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> getByTemplate (@PathVariable Long templateId){
        return new ResponseEntity<>(opdTempService.getByTemplateId(templateId), HttpStatus.OK);
    }

    @PostMapping("/create-opdTemplate")
    public ResponseEntity<ApiResponse<OpdTemplateResponse>> creatingOpdTemplate (@RequestBody OpdTemplateRequest opdTempReq){
        return new ResponseEntity<>(opdTempService.createOpdTemplate(opdTempReq), HttpStatus.CREATED);
    }


    @PutMapping("/update-opdTemplate/{templateId}")
    public ResponseEntity<ApiResponse<String>> updatingOpdTemplate (
            @RequestBody OpdTempInvReq opdTempInvReq ) {
        return new ResponseEntity<>(opdTempService.updateOpdTemplate(opdTempInvReq), HttpStatus.OK);
    }

    @PutMapping("/add-multi-investigation/{templateId}")
    public ResponseEntity<ApiResponse<InvestigationByTemplateResponse>> addingMultiInvestigation(
            @RequestBody InvestigationByTemplateRequest investByTempReq ) {
        return new ResponseEntity<>(opdTempService.multiInvestigationTemplate(investByTempReq), HttpStatus.OK);
    }
}
