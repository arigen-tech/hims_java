package com.hims.controller;

import com.hims.request.MasReligionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;
import com.hims.service.MasReligionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasReligionController", description = "Controller for handling Religion Master")
@RequestMapping("/religion")
public class MasReligionController {

    @Autowired
    private MasReligionService masReligionService;

    @GetMapping("/getAllReligions/{flag}")
    public ApiResponse<List<MasReligionResponse>> getAllReligions(@PathVariable int flag) {
        return masReligionService.getAllReligions(flag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> getReligionById(@PathVariable Long id) {
        ApiResponse<MasReligionResponse> response = masReligionService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasReligionResponse>> addReligion(@RequestBody MasReligionRequest religionRequest) {
        ApiResponse<MasReligionResponse> response = masReligionService.addReligion(religionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> updateReligion(
            @PathVariable Long id,
            @RequestBody MasReligionResponse religionDetails) {
        ApiResponse<MasReligionResponse> response = masReligionService.updateReligion(id, religionDetails);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasReligionResponse> response = masReligionService.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}