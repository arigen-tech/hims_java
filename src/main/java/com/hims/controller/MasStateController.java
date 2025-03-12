package com.hims.controller;

import com.hims.request.MasStateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.service.MasStateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasStateController", description = "Controller for handling State Master")
@RequestMapping("/state")
public class MasStateController {

    @Autowired
    private MasStateService masStateService;

    @GetMapping("/getAllStates/{flag}")
    public ApiResponse<List<MasStateResponse>> getAllStates(@PathVariable int flag) {
        return masStateService.getAllStates(flag);
    }

    @PostMapping("/create")
    public ApiResponse<MasStateResponse> addState(@RequestBody MasStateRequest request) {
        return masStateService.addState(request);
    }

    @PutMapping("/status/{id}")
    public ApiResponse<String> changeStateStatus(@PathVariable Long id, @RequestParam String status) {
        return masStateService.changeStateStatus(id, status);
    }

    @PutMapping("/edit/{id}")
    public ApiResponse<MasStateResponse> editState(@PathVariable Long id, @RequestBody MasStateRequest request) {
        return masStateService.editState(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<MasStateResponse> getStateById(@PathVariable Long id) {
        return masStateService.getStateById(id);
    }

    @GetMapping("/country/{countryId}")
    public ApiResponse<List<MasStateResponse>> getStatesByCountryId(@PathVariable Long countryId) {
        return masStateService.getStatesByCountryId(countryId);
    }
}
