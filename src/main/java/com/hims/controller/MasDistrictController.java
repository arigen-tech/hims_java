package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.service.MasDistrictService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasDistrictController", description = "Controller for handling District Master")
@RequestMapping("/district")
public class MasDistrictController {

    @Autowired
    private MasDistrictService masDistrictService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasDistrictResponse>>> getAllDistricts() {
        ApiResponse<List<MasDistrictResponse>> response = masDistrictService.getAllDistricts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
