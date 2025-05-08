package com.hims.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.Patient;
import com.hims.entity.Visit;
import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.PatientRegFollowUpResp;
import com.hims.service.PatientService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
@RequestMapping("/patient")
@Slf4j
public class PatientController {
    @Autowired
    PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> registerPatient(@RequestBody PatientRegistrationReq request) {
        ApiResponse<PatientRegFollowUpResp> response = patientService.registerPatientWithOpd(request.getPatient(), request.getOpdPatientDetail(),request.getVisit());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> updatePatient(@RequestBody PatientFollowUpReq request) {
        ApiResponse<PatientRegFollowUpResp> response = patientService.updatePatient(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            ApiResponse response = patientService.uploadImage(file);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(ResponseUtils.createFailureResponse(e.getMessage(), new TypeReference<String>() {
            },"Error uploading image",500), HttpStatus.OK);
        }
    }
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<Patient>>> searchPatient(@RequestBody PatientSearchReq searchRequest){
        ApiResponse<List<Patient>> response = patientService.searchPatient(searchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/getPendingPreConsultations")
    public ResponseEntity<ApiResponse<List<Visit>>> getPendingPreConsultations(){
        ApiResponse<List<Visit>> response = patientService.getPendingPreConsultations();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @PostMapping("/saveVitalDetails")
    public ResponseEntity<ApiResponse<String>> saveVitalDetails(@RequestBody OpdPatientDetailRequest request){
        ApiResponse<String> response=patientService.saveVitalDetails(request);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
