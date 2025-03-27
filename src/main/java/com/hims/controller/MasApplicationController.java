package com.hims.controller;

import com.hims.request.BatchUpdateRequest;
import com.hims.request.MasApplicationRequest;
import com.hims.request.UpdateStatusRequest;
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

    @GetMapping("/getAllApplications/{flag}")
    public ApiResponse<List<MasApplicationResponse>> getAllApplications(@PathVariable int flag) {
        return masApplicationService.getAllApplications(flag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> getApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(masApplicationService.getApplicationById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> createApplication(@RequestBody MasApplicationRequest request) {
        return ResponseEntity.status(201).body(masApplicationService.createApplication(request));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> updateApplication(@PathVariable String id, @RequestBody MasApplicationRequest request) {
        return ResponseEntity.ok(masApplicationService.updateApplication(id, request));
    }

    @GetMapping("/getAllChildren/{parentId}")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllByParentId(@PathVariable String parentId) {
        return new ResponseEntity<>(masApplicationService.getAllByParentId(parentId), HttpStatus.OK);
    }

    @PutMapping("/updateBatchStatus")
    public ResponseEntity<ApiResponse<String>> updateMultipleApplicationStatuses(@RequestBody UpdateStatusRequest request) {
        return new ResponseEntity<>(masApplicationService.updateMultipleApplicationStatuses(request), HttpStatus.OK);
    }

    @GetMapping("/getAllParents/{flag}")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllParentApplications(@PathVariable int flag) {
        return new ResponseEntity<>(masApplicationService.getAllParentApplications(flag), HttpStatus.OK);
    }

    @PostMapping("/assignUpdateTemplate")
    public ResponseEntity<ApiResponse<String>> processBatchUpdates(@RequestBody BatchUpdateRequest request) {
        ApiResponse<String> response = masApplicationService.processBatchUpdates(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}