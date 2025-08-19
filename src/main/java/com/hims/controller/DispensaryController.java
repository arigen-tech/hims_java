package com.hims.controller;

import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.OpeningBalanceEntryService;
import com.hims.service.PhysicalBatchStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@Tag(name = "DispensaryController")
@RequestMapping("/openingBalanceEntry")
public class DispensaryController {
    @Autowired
    private OpeningBalanceEntryService openingBalanceEntryService;
    @Autowired
    private PhysicalBatchStockService physicalBatchStockService;

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
    @GetMapping("/list/{status}/{hospitalId}/{departmentId}")
    public ResponseEntity<List<OpeningBalanceEntryResponse>> getListByStatus(@PathVariable String status,@PathVariable Long hospitalId,@PathVariable Long departmentId) {
        List<String> statusList = Arrays.asList(status.split(","));
        List<OpeningBalanceEntryResponse> responseList = openingBalanceEntryService.getListByStatus(statusList, hospitalId, departmentId);
        return ResponseEntity.ok(responseList);
    }
    @GetMapping("getDetailsById/{id}/{hospitalId}/{departmentId}")
    public ResponseEntity<ApiResponse<OpeningBalanceEntryResponse>> getDetailsById(@PathVariable Long id,@PathVariable Long hospitalId,@PathVariable Long departmentId) {
        return ResponseEntity.ok(openingBalanceEntryService.getDetailsById(id,hospitalId,departmentId));
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

    @GetMapping("getAllStock/{type}/{hospitalId}/{departmentId}")
    public ResponseEntity<ApiResponse<List<?>>>  getAllData(@PathVariable String type,@PathVariable Long hospitalId,@PathVariable Long departmentId) {
        return ResponseEntity.ok(openingBalanceEntryService.getAllStock(type, hospitalId, departmentId));

    }
    @PutMapping("/updateByMrp")
    public ResponseEntity<ApiResponse<String>> updateByMrp(@RequestBody List<UpdateMrpValue> marValue) {
        return ResponseEntity.ok(openingBalanceEntryService.updateByMrp(marValue));
    }

    @GetMapping("/stocks/{fromDate}/{toDate}/{itemId}/{hospitalId}/{departmentId}")
    public ResponseEntity<ApiResponse<List<OpeningBalanceStockResponse2 >>> getStockByDateRange(
            @PathVariable LocalDate fromDate, @PathVariable LocalDate toDate,@RequestParam(required = false) Long itemId,@PathVariable Long hospitalId,@PathVariable Long departmentId){
        ApiResponse<List<OpeningBalanceStockResponse2 >> response = openingBalanceEntryService.getStockByDateRange(fromDate, toDate,itemId,hospitalId,departmentId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/getStockByItemId/{itemId}/{hospitalId}/{departmentId}")
    public ResponseEntity<ApiResponse<List<OpeningBalanceStockResponse2 >>> getStockByItemId(
            @PathVariable  Long itemId,@PathVariable Long hospitalId,@PathVariable Long departmentId){
        ApiResponse<List<OpeningBalanceStockResponse2 >> response = openingBalanceEntryService.getStockByItemId(itemId, hospitalId, departmentId);
        return ResponseEntity.ok(response);
    }


    //   ========================================================Physical Stocks=====================================================


    @PostMapping("/createPhysicalStock")
    public ResponseEntity<ApiResponse<String>> createPhysicalStock(@RequestBody StoreStockTakingMRequest storeStockTakingM) {
        return new ResponseEntity<>(physicalBatchStockService.createPhysicalStock(storeStockTakingM), HttpStatus.CREATED);
    }
    @GetMapping("/listPhysical/{status}/{hospitalId}/{departmentId}")
    public ResponseEntity<List<StoreStockTakingMResponse>> getListByStatusPhysical(@PathVariable String status,@PathVariable Long hospitalId,@PathVariable Long departmentId) {
        List<String> statusList = Arrays.asList(status.split(","));
        List<StoreStockTakingMResponse> responseList = physicalBatchStockService.getListByStatusPhysical(statusList, hospitalId, departmentId);
        return ResponseEntity.ok(responseList);

    }
    @PutMapping("/updateStatusPhysicalById/{id}/{status}")
    public ResponseEntity<ApiResponse<String>> updateStatusPhysicalById(@PathVariable Long id, @PathVariable String status) {
        return ResponseEntity.ok(physicalBatchStockService.updateByStatus(id,status));

    }
    @PutMapping("/updatePhysicalById/{id}")
    public ResponseEntity<ApiResponse<String>> updatePhysicalById(@PathVariable Long id, @RequestBody StoreStockTakingMRequest storeStockTakingMRequest) {
        return ResponseEntity.ok(physicalBatchStockService.updatePhysicalById(id,storeStockTakingMRequest));
    }

    @PutMapping("/approvedPhysical")
    public ResponseEntity<ApiResponse<String>> approvedPhysical(@RequestBody StoreStockTakingMRequest2 request) {
        return new ResponseEntity<>(physicalBatchStockService.approvedPhysical(request),HttpStatus.CREATED);
    }

    // ===========================   Indent  ================================






}
