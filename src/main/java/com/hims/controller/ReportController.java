package com.hims.controller;

import com.hims.service.LabReportService;
import com.hims.service.OpdReportService;
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
}