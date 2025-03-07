package com.hims.controller;

import com.hims.request.MasBloodGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodGroupResponse;
import com.hims.service.MasBloodGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasBloodGroupController", description = "Controller for handling Blood Group Master")
@RequestMapping("/blood-group")
public class MasBloodGroupController {

    @Autowired
    private MasBloodGroupService masBloodGroupService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasBloodGroupResponse>>> getAllBloodGroups() {
        ApiResponse<List<MasBloodGroupResponse>> response = masBloodGroupService.getAllBloodGroups();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> getBloodGroupById(@PathVariable Long id) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> addBloodGroup(@RequestBody MasBloodGroupRequest bloodGroupRequest) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.addBloodGroup(bloodGroupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> updateBloodGroup(
            @PathVariable Long id,
            @RequestBody MasBloodGroupResponse bloodGroupDetails) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.updateBloodGroup(id, bloodGroupDetails);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}