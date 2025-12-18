package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.response.ApiResponse;
import com.hims.service.impl.ReportPrintFacade;
import com.hims.utils.JasperPrintUtil;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "JasperPrintController", description = "Controller for printing all reports")
@RequestMapping("/print")
public class JasperPrintController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JasperPrintUtil printUtil;

    @Autowired
    private ReportPrintFacade reportPrint;

    @PostMapping("/opd-casesheet")
    public ResponseEntity<String> printOpdCaseSheet(
            @RequestParam Long visitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("visit_id", visitId);
        params.put("SUBREPORT_DIR", getClass()
                .getResource("/jasperReport/opdCaseSheetReport/")
                .toString() );
        params.put("path", getClass()
                .getResource("/Assets/arigen_health.png")
                .toString() );
        try (Connection conn = dataSource.getConnection()) {
            reportPrint.printJasperReport("opd_casesheet_new_report_1.jasper", params, conn);
            return ResponseEntity.ok("OPD Case Sheet Printed");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Printing failed: " + e.getMessage());
        }
    }

    @PostMapping("lab-investigation")
    public ResponseEntity<?> printLabReport(
            @RequestParam Integer orderhd_id ) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderhd_id", orderhd_id);
        try (Connection conn = dataSource.getConnection()) {
            reportPrint.printJasperReport("Lab_investigation_report.jasper", params, conn);
            return ResponseEntity.ok(
                    ResponseUtils.createSuccessResponse("Lab Report printed successfully", new TypeReference<>() {}));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Printing failed: " + e.getMessage());
        }
    }
}
