package com.hims.controller;

import com.hims.request.MasInvestigationPriceDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationPriceDetailsProjectionResponse;
import com.hims.response.MasInvestigationPriceDetailsResponse;
import com.hims.service.MasInvestigationPriceDetailsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasInvestigationPriceDetailsController", description = "Controller for handling Investigation Price Details")
@RequestMapping("/investigation-price-details")
public class MasInvestigationPriceDetailsController {

    @Autowired
    private MasInvestigationPriceDetailsService priceDetailsService;

    @GetMapping("/getAllPriceDetails/{flag}")
    public ApiResponse<Page<MasInvestigationPriceDetailsProjectionResponse>> getAllPriceDetails(
            @PathVariable int flag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String investigationName) {
        return priceDetailsService.getAllPriceDetails(flag, page, size, investigationName);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasInvestigationPriceDetailsResponse>> getPriceDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(priceDetailsService.findById(id));
    }

    @GetMapping("/investigation/{investigationId}")
    public ResponseEntity<ApiResponse<List<MasInvestigationPriceDetailsResponse>>> getPriceDetailsByInvestigationId(
            @PathVariable Long investigationId) {
        return ResponseEntity.ok(priceDetailsService.findByInvestigationId(investigationId));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasInvestigationPriceDetailsResponse>> addPriceDetails(
            @RequestBody MasInvestigationPriceDetailsRequest request) {
        ApiResponse<MasInvestigationPriceDetailsResponse> response = priceDetailsService.addPriceDetails(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasInvestigationPriceDetailsResponse>> updatePriceDetails(
            @PathVariable Long id,
            @RequestBody MasInvestigationPriceDetailsRequest request) {
        ApiResponse<MasInvestigationPriceDetailsResponse> response = priceDetailsService.updatePriceDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasInvestigationPriceDetailsResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(priceDetailsService.changeStatus(id, status));
    }
}
