package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;
import com.hims.service.MasMaritalStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasMaritalStatusController", description = "Controller for handling Marital Status Master")
@RequestMapping("/marital-status")
public class MasMaritalStatusController {

    @Autowired
    private MasMaritalStatusService masMaritalStatusService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasMaritalStatusResponse>>> getAllMaritalStatuses() {
        ApiResponse<List<MasMaritalStatusResponse>> response = masMaritalStatusService.getAllMaritalStatuses();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
