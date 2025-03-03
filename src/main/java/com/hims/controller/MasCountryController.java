package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;
import com.hims.service.MasCountryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasCountryController", description = "Controller for handling Country Master")
@RequestMapping("/country")
public class MasCountryController {

    @Autowired
    private MasCountryService masCountryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasCountryResponse>>> getAllCountries() {
        ApiResponse<List<MasCountryResponse>> response = masCountryService.getAllCountries();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
