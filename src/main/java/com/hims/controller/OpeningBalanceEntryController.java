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
import java.util.List;

@RestController
@Tag(name = "OpeningBalanceEntryController")
@RequestMapping("/openingBalanceEntry")
public class OpeningBalanceEntryController {
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
    @PutMapping("/updateByMrp")
    public ResponseEntity<ApiResponse<String>> updateByMrp(@RequestBody List<UpdateMrpValue> marValue) {
        return ResponseEntity.ok(openingBalanceEntryService.updateByMrp(marValue));
    }

    @GetMapping("/stocks/{fromDate}/{toDate}/{itemId}")
    public ResponseEntity<ApiResponse<List<OpeningBalanceStockResponse2 >>> getStockByDateRange(
            @PathVariable LocalDate fromDate, @PathVariable LocalDate toDate,@RequestParam(required = false) Long itemId){
        ApiResponse<List<OpeningBalanceStockResponse2 >> response = openingBalanceEntryService.getStockByDateRange(fromDate, toDate,itemId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/getStockByItemId/{itemId}")
    public ResponseEntity<ApiResponse<List<OpeningBalanceStockResponse2 >>> getStockByItemId(
            @PathVariable  Long itemId){
        ApiResponse<List<OpeningBalanceStockResponse2 >> response = openingBalanceEntryService.getStockByItemId(itemId);
        return ResponseEntity.ok(response);
    }


 //   ===============================================================================================================================================


    @PostMapping("/createPhysicalStock")
    public ResponseEntity<ApiResponse<String>> createPhysicalStock(@RequestBody StoreStockTakingMRequest storeStockTakingM) {

        return new ResponseEntity<>(physicalBatchStockService.createPhysicalStock(storeStockTakingM), HttpStatus.CREATED);
    }
    @GetMapping("/listPhysical/{status}")
    public ResponseEntity<List<StoreStockTakingMResponse>> getListByStatusPhysical(@PathVariable String status) {
        String[] statuses = status.split(",");
        return ResponseEntity.ok(physicalBatchStockService.getListByStatusPhysical(statuses));
    }
    @PutMapping("/updateStatusPhysicalById/{id}")
    public ResponseEntity<ApiResponse<String>> updateStatusPhysicalById(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(physicalBatchStockService.updateByStatus(id,status));

    }




}
