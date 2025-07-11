package com.hims.controller;

import com.hims.entity.MasManufacturer;
import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreItemBatchStock;
import com.hims.request.MasManufacturerRequest;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.request.OpeningBalanceEntryRequest2;
import com.hims.response.*;
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

    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<String>> updateOpeningBalance(@PathVariable Long id,
            @RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return ResponseEntity.ok(openingBalanceEntryService.update(id,openingBalanceEntryRequest));
    }
    @PutMapping("/updateStatusById/{id}")
    public ResponseEntity<ApiResponse<String>> updateStatusById(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(openingBalanceEntryService.updateByStatus(id,status));

    }
    @GetMapping("/list/{status}")
    public ResponseEntity<List<OpeningBalanceEntryResponse>> getListByStatus(@PathVariable String status) {
        String[] statuses = status.split(",");
        return ResponseEntity.ok(openingBalanceEntryService.getListByStatus(statuses));
    }
    @GetMapping("getDetailsById/{id}")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> getDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(openingBalanceEntryService.getDetailsById(id));
    }


    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> createAndUpdateStatus(
            @RequestBody OpeningBalanceEntryRequest request
            ) {
        ApiResponse<OpeningBalanceEntryResponse> response =openingBalanceEntryService.createAndUpdateStatus(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PutMapping("/Approved/{id}")
    public ResponseEntity<ApiResponse<String>> Approved(@PathVariable Long id,
            @RequestBody OpeningBalanceEntryRequest2 request
    ) {
        openingBalanceEntryService.approved(id,request);
        return new ResponseEntity<>(openingBalanceEntryService.approved(id,request), HttpStatus.CREATED);
    }

    @GetMapping("getAllStock/{type}")
    public ResponseEntity<ApiResponse<List<?>>>  getAllData(@PathVariable String type) {
        return ResponseEntity.ok(openingBalanceEntryService.getAllStock(type));

    }









}
