package com.hims.controller;

import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.service.UserApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "UserApplicationController", description = "Controller for handling User Applications")
@RequestMapping("/applications")
public class UserApplicationController {

    @Autowired
    private UserApplicationService userApplicationService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserApplicationResponse>>> getAllApplications() {
        return new ResponseEntity<>(userApplicationService.getAllApplications(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> getApplicationById(@PathVariable Long id) {
        return new ResponseEntity<>(userApplicationService.getApplicationById(id), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> createApplication(@RequestBody UserApplicationRequest request) {
        return new ResponseEntity<>(userApplicationService.createApplication(request), HttpStatus.CREATED);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeApplicationStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(userApplicationService.changeApplicationStatus(id, status), HttpStatus.OK);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> editApplication(@PathVariable Long id, @RequestBody UserApplicationRequest request) {
        return new ResponseEntity<>(userApplicationService.updateApplication(id, request), HttpStatus.OK);
    }
}