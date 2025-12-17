package com.hims.controller;

import com.hims.service.OpdCaseSheetService;
import com.hims.utils.JasperPrintUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "JasperPrintController", description = "Controller for printing all reports")
@RequestMapping("/print")
public class JasperPrintController {

    @Autowired
    private OpdCaseSheetService opdCaseSheetService;

    @Autowired
    private JasperPrintUtil printUtil;

    @SneakyThrows
    @PostMapping("/opd-casesheet")
    public ResponseEntity<String> printOpdCaseSheet(
            @RequestParam Long visitId) {
        JasperPrint jasperPrint = opdCaseSheetService.printOpdCaseSheetReport(visitId);
        printUtil.print(jasperPrint);
        return ResponseEntity.ok("Printed successfully");
    }
}
