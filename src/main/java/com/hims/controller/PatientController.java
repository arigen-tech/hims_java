package com.hims.controller;


import com.hims.entity.Patient;
import com.hims.request.PatientRegistrationReq;
import com.hims.request.PatientRequest;
import com.hims.response.ApiResponse;
import com.hims.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
@RequestMapping("/patient")
@Slf4j
public class PatientController {
    @Autowired
    PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Patient>> registerPatient(@RequestBody PatientRegistrationReq request) {
        ApiResponse<Patient> response = patientService.registerPatientWithOpd(request.getPatient(), request.getOpdPatientDetail(),request.getVisit());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Patient>> updatePatient(@RequestBody PatientRequest request) {
        ApiResponse<Patient> response = patientService.updatePatient(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
