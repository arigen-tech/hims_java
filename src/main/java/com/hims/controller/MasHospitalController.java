package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.service.MasHospitalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasHospitalController", description = "Controller for handling Hospital Master")
@RequestMapping("/hospital")
public class MasHospitalController {

    @Autowired
    private MasHospitalService masHospitalService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasHospitalResponse>>> getAllHospitals() {
        ApiResponse<List<MasHospitalResponse>> response = masHospitalService.getAllHospitals();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
