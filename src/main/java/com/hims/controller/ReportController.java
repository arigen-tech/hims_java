package com.hims.controller;

import com.hims.constants.ReportConstants;
import com.hims.utils.JasperReportUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@Tag(name = "ReportController", description = "Controller for handling All Reports")
@RequestMapping("/report")
@Slf4j
public class ReportController {

    @Autowired
    private DataSource dataSource;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @GetMapping(value = "/labReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintLabReportPdf(
            @RequestParam String billNo,
            @RequestParam String paymentStatus,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("Bill_no", billNo);
        params.put("Pay_status", paymentStatus);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.LAB_REPORT);
            } else if ("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/opdReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdReportPdf(
            @RequestParam Long visit ,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("visit_id", visit);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_REPORT);
            } else if ("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }

    }

    @GetMapping(value = "/stockReportSummary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockReportSummaryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Integer itemClassId,
            @RequestParam Integer sectionId,
            @RequestParam Long itemId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("ITEM_CLASS_ID", itemClassId);
        params.put("SECTION_ID", sectionId);
        params.put("ITEM_ID", itemId);
        params.put("CurrentDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_SUMMARY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_SUMMARY_REPORT);
            } else if ("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_SUMMARY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }

    }

    @GetMapping(value = "/stockReportDetail", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockReportDetailPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Integer itemClassId,
            @RequestParam Integer sectionId,
            @RequestParam Long itemId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("ITEM_CLASS_ID", itemClassId);
        params.put("SECTION_ID", sectionId);
        params.put("ITEM_ID", itemId);
        params.put("CurrentDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_STATUS_DETAILED_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_STATUS_DETAILED_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_STATUS_DETAILED_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }

    }

    @GetMapping(value = "/openingBalanceRegistryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public  ResponseEntity<?> viewPrintOpeningBalanceRegistryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("department_id", departmentId);
        params.put("FromDate", fromDate);
        params.put("ToDate", toDate);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_REGISTRY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPENING_BALANCE_REGISTRY_REPORT);
            }
            else if("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_REGISTRY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }

    }

    @GetMapping(value = "/openingBalanceReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpeningBalanceReportPdf(
            @RequestParam Long balanceMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("balance_m_id", balanceMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPENING_BALANCE_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Opening Balance report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/stockTakingRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockTakingRegisterPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("FromDate", fromDate);
        params.put("ToDate", toDate);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_TAKING_REGISTER_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Stock Taking Register: " + e.getMessage());
        }
    }

    @GetMapping(value = "/stockTakingReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockTakingReportPdf(
            @RequestParam Long takingMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("TAKING_M_ID", takingMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_TAKING_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Stock taking Report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/drugExpiryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintDrugExpiryReportPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("ITEM_ID", itemId);
        params.put("FromDate", fromDate);
        params.put("ToDate", toDate);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DRUG_EXPIRY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DRUG_EXPIRY_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DRUG_EXPIRY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Drug Expiry Report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintIndentReportPdf(
            @RequestParam Long indentMId ,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("indent_m_id", indentMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO
                .toString()));
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Lab investigation report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/opdToken", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdTokenPdf(
            @RequestParam Long visit ,
            @RequestParam String flag) {
        Map<String , Object> params = new HashMap<>();
        params.put("visit_id",visit);
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_TOKEN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_TOKEN_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_TOKEN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Lab investigation report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/labInvestigationReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintLabInvestigationReport(
            @RequestParam Integer orderhd_id,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("orderhd_id", orderhd_id);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB,ReportConstants.LAB_INVESTIGATION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.LAB_INVESTIGATION_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB,ReportConstants.LAB_INVESTIGATION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Lab investigation report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/opdCaseSheetReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdCaseSheetReport(
            @RequestParam Long visitId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("visit_id", visitId);
        params.put("SUBREPORT_DIR", getClass()
                .getResource(ReportConstants.JASPER_BASE_PATH_OPD + ReportConstants.OPD_SUBREPORT_DIR)
                .toString());
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());

        try {
            if ("D".equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_CASESHEET_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_CASESHEET_REPORT);
            } else if ("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_CASESHEET_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate OPD case sheet: " + e.getMessage());
        }
    }

    @GetMapping(value = "/indentMedicineIssueRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintMedicineIssueRegisterReport(
            @RequestParam Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag ) {
        Map<String, Object> params = new HashMap<>();
        params.put("drug_id", itemId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("SUBREPORT_DIR", getClass()
                .getResource(ReportConstants.JASPER_BASE_PATH_STORE + ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_SUBREPORT_DIR)
                .toString());
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO
                .toString()));

        try{
            if ("D".equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_REPORT);
            } else if ("P".equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate : " + e.getMessage());
            }
    }

    @GetMapping(value = "/indentIssue", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadIndentIssue(
            @RequestParam Long issueMId,
            @RequestParam String flag ) {
        Map<String, Object> params = new HashMap<>();
        params.put("issue_m_id", issueMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO
                .toString()));
        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_ISSUE_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_ISSUE_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_ISSUE_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Indent issue report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadIndentReceiving(
            @RequestParam Long indentMId,
            @RequestParam String flag ) {
        Map<String, Object> params = new HashMap<>();
        params.put("indent_m_id", indentMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_RECEIVING_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Indent receiving report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/labRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadLabRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long genderId,
            @RequestParam Long investigationId,
            @RequestParam Long fromAge,
            @RequestParam Long toAge ,
            @RequestParam String flag) {
        Long safeGenderId = (genderId == null ? 0L : genderId);
        Long safeInvestigationId = (investigationId == null ? 0L : investigationId);
        Long safeFromAge = (fromAge == null ? 0L : fromAge);
        Long safeToAge = (toAge == null ? 0L : toAge);

        Map<String, Object> params =  new HashMap<>();
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("gender_id", safeGenderId);
        params.put("investigation_id", safeInvestigationId);
        params.put("from_age", safeFromAge);
        params.put("to_age", safeToAge);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        params.put("SUBREPORT_DIR", getClass()
                .getResource(ReportConstants.JASPER_BASE_PATH_LAB + ReportConstants.LAB_REGISTER_SUB_REPORT_DIR)
                .toString());

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.LAB_REGISTER_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Lab Register report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/itemWiseReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadItemWiseReceiving(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long itemId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("item_id", itemId);

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.ITEM_WISE_RECEIVING_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Item Wise Receiving report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/dateWiseReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadDateWiseReceiving(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());
        params.put("from_date", fromDate);
        params.put("to_date", toDate);

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DATE_WISE_RECEIVING_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Date Wise Receiving report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReturn", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadIndentReturn(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long returnMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("return_m_id", returnMId);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.INDENT_RETURN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_RETURN_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.INDENT_RETURN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate item return report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/itemWiseReturn", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadItemWiseReturn(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("item_id", itemId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("path", getClass()
                .getResource(ReportConstants.ASSET_LOGO)
                .toString());

        try{
            if ("D".equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RETURN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.ITEM_WISE_RETURN_REPORT);
            } else if ("P".equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RETURN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                "Invalid flag value. Use D or P", 400));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Item Wise Return report: " + e.getMessage());
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(
            byte[] pdfData,
            String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline" + fileName + ".pdf")
                .body(pdfData);
    }
}