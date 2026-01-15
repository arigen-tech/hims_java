package com.hims.controller;


import com.hims.service.LabReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportGetApiController {

    private  final LabReportService labReportService;

    @GetMapping("/lab-history/all")
    public ResponseEntity<?> searchLabReports(
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String patientName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return ResponseEntity.ok(labReportService.getAllLabReports(mobileNo, patientName, fromDate, toDate));
    }

    @GetMapping("/lab-tat/details")
    public ResponseEntity<?> getAllLabReports(
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subchargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

              return  ResponseEntity.ok( labReportService.getDetailedTatReports(investigationId, subchargeCodeId, fromDate, toDate));
    }

    @GetMapping("/lab-tat/summary")
    public ResponseEntity<?> getTatSummaryLabReports(
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subchargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return  ResponseEntity.ok( labReportService.getSummaryTatReports(investigationId, subchargeCodeId, fromDate, toDate));
    }

    @GetMapping("/lab-amend-audit")
    public ResponseEntity<?> getAmendAuditReports(
            @RequestParam(required = false) String phnNum,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(labReportService.getAmendAuditReports(phnNum, patientName, investigationId, subChargeCodeId, fromDate, toDate));
    }

    @GetMapping("/order-track-report")
    public ResponseEntity<?> getOrderTrackingReport(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(labReportService.getOrderTrackingReports(patientName, mobileNo, fromDate, toDate));
    }

    @GetMapping("/incomplete-investigation-report")
    public ResponseEntity<?> getIncompleteInvestigationsReport(
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
    {
       return ResponseEntity.ok(labReportService.getIncompleteInvestigationReports(subChargeCodeId,fromDate,toDate));
    }

    @GetMapping("/reject-investigation-report")
    public ResponseEntity<?> getRejectInvestigationReport(
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
    {
        return ResponseEntity.ok(labReportService.getSampleRejectionReport(subChargeCodeId,fromDate,toDate));
    }


}
