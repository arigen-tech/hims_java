package com.hims.controller;

import com.hims.request.MasDistrictRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.service.MasDistrictService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> addDistrict(@RequestBody MasDistrictRequest request) {
        return ResponseEntity.ok(masDistrictService.addDistrict(request));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDistrictStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDistrictService.changeDistrictStatus(id, status));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> editDistrict(@PathVariable Long id, @RequestBody MasDistrictRequest request) {
        return ResponseEntity.ok(masDistrictService.editDistrict(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(masDistrictService.getDistrictById(id));
    }

    @GetMapping("/state/{stateId}")
    public ResponseEntity<ApiResponse<List<MasDistrictResponse>>> getDistrictsByStateId(@PathVariable Long stateId) {
        return ResponseEntity.ok(masDistrictService.getDistrictsByStateId(stateId));
    }
}
