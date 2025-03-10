package com.hims.controller;

import com.hims.request.MasCountryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;
import com.hims.service.MasCountryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    public ApiResponse<MasCountryResponse> addCountry(@RequestBody MasCountryRequest request) {
        return masCountryService.addCountry(request);
    }

    @PutMapping("/status/{id}")
    public ApiResponse<String> changeCountryStatus(@PathVariable Long id, @RequestParam String status) {
        return masCountryService.changeCountryStatus(id, status);
    }

    @PutMapping("/edit/{id}")
    public ApiResponse<MasCountryResponse> editCountry(@PathVariable Long id, @RequestBody MasCountryRequest request) {
        return masCountryService.editCountry(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<MasCountryResponse> getCountryById(@PathVariable Long id) {
        return masCountryService.getCountryById(id);
    }
}
