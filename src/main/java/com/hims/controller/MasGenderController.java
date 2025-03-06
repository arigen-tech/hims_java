package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasGender;
import com.hims.helperUtil.ResponseUtils;
import com.hims.request.MasGenderRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasGenderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasGenderController", description = "Controller for handling Gender Master")
@RequestMapping("/gender")
public class MasGenderController {

    @Autowired
    private MasGenderService masGenderService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasGenderResponse>>> getAllGenders() {
        ApiResponse<List<MasGenderResponse>> response = masGenderService.getAllGenders();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> getGenderById(@PathVariable Long id) {
        ApiResponse<MasGenderResponse> response = masGenderService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasGenderResponse>> addGender(@RequestBody MasGenderRequest genderRequest) {
        ApiResponse<MasGenderResponse> response = masGenderService.addGender(genderRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update an existing gender by ID
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> updateGender(
            @PathVariable Long id,
            @RequestBody MasGenderResponse genderDetails) {
        ApiResponse<MasGenderResponse> response = masGenderService.updateGender(id, genderDetails);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Change status of a gender (Active: "Y", Inactive: "N")
    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasGenderResponse> response = masGenderService.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
