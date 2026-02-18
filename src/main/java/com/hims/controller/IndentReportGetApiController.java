package com.hims.controller;

import com.hims.service.IndentReportGetApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/indent")
@RequiredArgsConstructor
public class IndentReportGetApiController {


    private final IndentReportGetApiService indentService;

    @GetMapping("/tracking")
    public ResponseEntity<?> getIndentTracking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(indentService.getIndentTrackingList(page, size));
    }

    @GetMapping("/tracking/search")
    public ResponseEntity<?> searchIndentTrackingList(
            @RequestParam(required = false) Long fromDepartmentId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

         return ResponseEntity.ok(
                 indentService.searchIndentTrackingList(
                                                            fromDepartmentId,
                                                            fromDate,
                                                            toDate,
                                                            page,
                                                            size
                )
         );
    }

    @GetMapping("/status-map")
    public ResponseEntity<?> getStatusMap(){
        return  ResponseEntity.ok(indentService.getStatusMapForIndentTracking());
    }

    @GetMapping("/get-issueMId")
    public ResponseEntity<?> getIssueMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(indentService.getIssueMIdFromIndentMId(indentMId));
    }

    @GetMapping("/get-receiveMId")
    public ResponseEntity<?> getReceiveMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(indentService.getReceiveMIdFromIndentMId(indentMId));
    }
    @GetMapping("/get-returnMId")
    public ResponseEntity<?> getReturnMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(indentService.getReturnMIdFromIndentMId(indentMId));
    }

    @GetMapping("/item/search")
    public ResponseEntity<?> getStockLedger(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(indentService.getStoreItems(keyword, page, size));
    }

    @GetMapping("/item/batches/{itemId}")
    public ResponseEntity<?> getAllBatchesWrtItem(@PathVariable Long itemId){
        return  ResponseEntity.ok(indentService.getBatchesFromItemId(itemId));
    }

    @GetMapping("/store-stock-ledger/report")
    public ResponseEntity<?> getStoreStockLedgerReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam Long itemId,
            @RequestParam String batchNo
    ) {
        return ResponseEntity.ok(indentService.getStoreStockLedgerReport(
                                                                                page,
                                                                                size,
                                                                                itemId,
                                                                                batchNo
                )
        );
    }
}
