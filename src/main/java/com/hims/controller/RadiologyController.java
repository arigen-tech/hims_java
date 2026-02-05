package com.hims.controller;

import com.hims.request.PatientRegistrationReq;
import com.hims.request.RadRegInvReq;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.PatientRegFollowUpResp;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.service.RadiologyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
