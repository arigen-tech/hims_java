package com.hims.controller;

import com.hims.request.MasApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;
import com.hims.service.MasApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasApplicationController", description = "Controller for handling Mas Applications")
@RequestMapping("/mas-applications")
public class MasApplicationController {

    @Autowired
    private MasApplicationService masApplicationService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllApplications() {
        return new ResponseEntity<>(masApplicationService.getAllApplications(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> getApplicationById(@PathVariable String id) {
        return new ResponseEntity<>(masApplicationService.getApplicationById(id), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> createApplication(@RequestBody MasApplicationRequest request) {
        return new ResponseEntity<>(masApplicationService.createApplication(request), HttpStatus.CREATED);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> updateApplication(@PathVariable String id, @RequestBody MasApplicationRequest request) {
        return new ResponseEntity<>(masApplicationService.updateApplication(id, request), HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeApplicationStatus(@PathVariable String id, @RequestParam String status) {
        return new ResponseEntity<>(masApplicationService.changeApplicationStatus(id, status), HttpStatus.OK);
    }
}