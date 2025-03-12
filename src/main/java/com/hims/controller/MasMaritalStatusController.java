package com.hims.controller;

import com.hims.request.MasMaritalStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;
import com.hims.service.MasMaritalStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasMaritalStatusController", description = "Controller for handling Marital Status Master")
@RequestMapping("/marital-status")
public class MasMaritalStatusController {

    @Autowired
    private MasMaritalStatusService masMaritalStatusService;

    @PostMapping("/create")
    public ApiResponse<MasMaritalStatusResponse> addMaritalStatus(@RequestBody MasMaritalStatusRequest request) {
        return masMaritalStatusService.addMaritalStatus(request);
    }

    @PutMapping("/status/{id}")
    public ApiResponse<String> changeMaritalStatus(@PathVariable Long id, @RequestParam String status) {
        return masMaritalStatusService.changeMaritalStatus(id, status);
    }

    @PutMapping("edit/{id}")
    public ApiResponse<MasMaritalStatusResponse> editMaritalStatus(@PathVariable Long id, @RequestBody MasMaritalStatusRequest request) {
        return masMaritalStatusService.editMaritalStatus(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<MasMaritalStatusResponse> getMaritalStatusById(@PathVariable Long id) {
        return masMaritalStatusService.getMaritalStatusById(id);
    }

    @GetMapping("/getAllMaritalStatuses/{flag}")
    public ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses(@PathVariable int flag) {
        return masMaritalStatusService.getAllMaritalStatuses(flag);
    }
}
