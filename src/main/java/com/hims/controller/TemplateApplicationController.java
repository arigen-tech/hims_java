package com.hims.controller;

import com.hims.request.TemplateApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.TemplateApplicationResponse;
import com.hims.service.TemplateApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "TemplateApplicationController", description = "Controller for handling Template Applications")
@RequestMapping("/template-applications")
public class TemplateApplicationController {

    @Autowired
    private TemplateApplicationService templateApplicationService;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<TemplateApplicationResponse>> assignTemplateToApplication(@RequestBody TemplateApplicationRequest request) {
        ApiResponse<TemplateApplicationResponse> response = templateApplicationService.assignTemplateToApplication(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/change-status")
    public ResponseEntity<ApiResponse<String>> changeTemplateApplicationStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        ApiResponse<String> response = templateApplicationService.changeTemplateApplicationStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TemplateApplicationResponse>>> getAllTemplateApplications() {
        ApiResponse<List<TemplateApplicationResponse>> response = templateApplicationService.getAllTemplateApplications();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
