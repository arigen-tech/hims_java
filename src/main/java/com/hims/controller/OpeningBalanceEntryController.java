package com.hims.controller;

import com.hims.entity.MasManufacturer;
import com.hims.entity.MasStoreItem;
import com.hims.request.MasManufacturerRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.MasInvestigationPriceDetailsResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.service.OpeningBalanceEntryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "OpeningBalanceEntryController")
@RequestMapping("/openingBalanceEntry")
public class OpeningBalanceEntryController {
    @Autowired
    private OpeningBalanceEntryService openingBalanceEntryService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> addOpeningBalanceEntry(@RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        ApiResponse<OpeningBalanceEntryResponse> response = openingBalanceEntryService.add(openingBalanceEntryRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> updateOpeningBalance(@PathVariable Long id,
            @RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return ResponseEntity.ok(openingBalanceEntryService.update(id,openingBalanceEntryRequest));
    }
    @PutMapping("/updateByStatus/{id}")
    public ResponseEntity<ApiResponse<String>> updateByStatus(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(openingBalanceEntryService.updateByStatus(id,status));

    }
    @GetMapping("/list/{status}")
    public ResponseEntity<List<OpeningBalanceEntryResponse>> getListByStatus(@PathVariable String status) {
        return ResponseEntity.ok(openingBalanceEntryService.getListByStatus(status));
    } @GetMapping("/{id}")
    public ResponseEntity<OpeningBalanceEntryResponse> getDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(openingBalanceEntryService.getDetailsById(id));
    }
    @PostMapping("/create-and-update-status/{status}")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> createAndUpdateStatus(
            @RequestBody OpeningBalanceEntryRequest request,
            @PathVariable String status) {
        ApiResponse<OpeningBalanceEntryResponse> response =openingBalanceEntryService.createAndUpdateStatus(request, status);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }










}
