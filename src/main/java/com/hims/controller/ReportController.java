package com.hims.controller;

import com.hims.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;


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

    @Autowired
    private OpeningBalanceRegistryReportService openBalanceRegistryService;

    @Autowired
    private OpeningBalanceReportService openBalanceReportService;

    @Autowired
    private StockTakingRegisterReportService stockTakingRegisterReportService;

    @Autowired
    private StockTakingReportService stockTakingReportService;

    @Autowired
    private DrugExpiryReportService expiryService;

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
            @RequestParam Integer itemClassId,
            @RequestParam Integer sectionId,
            @RequestParam Long itemId) {
        return stockService.generateStockSummaryReport(hospitalId, departmentId, itemClassId, sectionId, itemId);
    }

    @GetMapping(value = "/stockReportDetail", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStockReportDetailPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Integer itemClassId,
            @RequestParam Integer sectionId,
            @RequestParam Long itemId) {
        return stockService.generateStockDetailedReport(hospitalId, departmentId, itemClassId, sectionId, itemId);
    }

    @GetMapping(value = "/openingBalanceRegistryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public  ResponseEntity<byte[]> generateOpeningBalanceRegistryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
        return openBalanceRegistryService.generateOpeningBalanceRegistry(hospitalId, departmentId, fromDate, toDate);
    }

    @GetMapping(value = "/openingBalanceReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateOpeningBalanceReportPdf(
            @RequestParam Long balanceMId) {
        return openBalanceReportService.generateOpeningBalanceReport(balanceMId);
    }

    @GetMapping(value = "/stockTakingRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStockTakingRegisterPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
        return stockTakingRegisterReportService.generateStockTakingRegistry(hospitalId, departmentId, fromDate, toDate);
    }

    @GetMapping(value = "/stockTakingReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStockTakingReportPdf(
            @RequestParam Long takingMId) {
        return stockTakingReportService.generateStockTaking(takingMId);
    }

    @GetMapping(value = "/drugExpiryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateDrugExpiryReportPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long itemId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
        return expiryService.generateDrugExpiryReport(hospitalId, departmentId, itemId, fromDate, toDate);
    }

    @GetMapping(value = "/indentReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateIndentReportPdf(
            @RequestParam Long indentMId ) {
        return null;
    }
}