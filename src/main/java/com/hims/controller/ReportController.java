package com.hims.controller;

import com.hims.service.LabReportService;
import com.hims.service.OpdReportService;
import com.hims.service.StockStatusReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "ReportController", description = "Controller for handling All Reports")
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private LabReportService labService;

    @Autowired
    private OpdReportService opdService;

    @Autowired
    private StockStatusReportService stockService;

    @GetMapping(value = "/labReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateLabReportPdf(
            @RequestParam String billNo,
            @RequestParam String paymentStatus) {
        return labService.generateLabReport(billNo, paymentStatus);
    }

    @GetMapping(value = "/opdReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateOpdReportPdf(
            @RequestParam Long visit ) {
        return opdService.generateOpdReport(visit);
    }

    @GetMapping(value = "/stockReportSummary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStockReportSummaryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam String path ) {
        return stockService.generateStockSummaryReport(hospitalId, departmentId, path);
    }

    @GetMapping(value = "/stockReportDetail", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStockReportDetailPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam String path ) {
        return stockService.generateStockDetailedReport(hospitalId, departmentId, path);
    }
}