package com.hims.controller;

import com.hims.request.MasHospitalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.service.MasHospitalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasHospitalController", description = "Controller for handling Hospital Master")
@RequestMapping("/hospital")
public class MasHospitalController {

    @Autowired
    private MasHospitalService masHospitalService;

    @GetMapping("/getAllHospitals/{flag}")
    public ApiResponse<List<MasHospitalResponse>> getAllHospitals(@PathVariable int flag) {
        return masHospitalService.getAllHospitals(flag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> getHospitalById(@PathVariable Long id) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> addHospital(@RequestBody MasHospitalRequest hospitalRequest) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.addHospital(hospitalRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> updateHospital(
            @PathVariable Long id,
            @RequestBody MasHospitalRequest hospitalRequest) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.updateHospital(id, hospitalRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}