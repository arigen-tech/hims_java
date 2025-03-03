package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;
import com.hims.service.MasReligionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasReligionController", description = "Controller for handling Religion Master")
@RequestMapping("/religion")
public class MasReligionController {

    @Autowired
    private MasReligionService masReligionService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasReligionResponse>>> getAllReligions() {
        ApiResponse<List<MasReligionResponse>> response = masReligionService.getAllReligions();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
