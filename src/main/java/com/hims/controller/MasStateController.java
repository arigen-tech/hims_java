package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.service.MasStateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasStateController", description = "Controller for handling State Master")
@RequestMapping("/state")
public class MasStateController {

    @Autowired
    private MasStateService masStateService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasStateResponse>>> getAllStates() {
        ApiResponse<List<MasStateResponse>> response = masStateService.getAllStates();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
