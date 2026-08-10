package com.hims.controller;


import com.hims.constants.ReportConstants;
import com.hims.utils.JasperReportUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@Tag(name = "ReportController", description = "Controller for handling All Reports")
@RequestMapping("/report")
@Slf4j
public class ReportController {

    @Autowired
    private DataSource dataSource;
    @Value("${labInvestigation.mainChargecodeId}")
    private Long mainChargecodeId;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @GetMapping(value = "/labInvoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintLabReportPdf(
            @RequestParam String billNo,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("Bill_no", billNo);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.LAB_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/opdInvoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdReportPdf(
            @RequestParam Long visit ,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("visit_id", visit);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }

    }

    @GetMapping(value = "/stockReportSummary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockReportSummaryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Integer itemClassId,
            @RequestParam Integer sectionId,
            @RequestParam Integer itemId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("ITEM_CLASS_ID", itemClassId);
        params.put("SECTION_ID", sectionId);
        params.put("ITEM_ID", itemId);
        params.put("CurrentDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_SUMMARY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_SUMMARY_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_SUMMARY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
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
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_STATUS_DETAILED_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_STATUS_DETAILED_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_STATUS_DETAILED_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }

    }

    @GetMapping(value = "/openingBalanceRegistryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public  ResponseEntity<?> viewPrintOpeningBalanceRegistryPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String balanceType,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("department_id", departmentId);
        params.put("FromDate", fromDate);
        params.put("ToDate", toDate);
        params.put("balance_type", balanceType);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_REGISTRY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPENING_BALANCE_REGISTRY_REPORT);
            }
            else if(ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_REGISTRY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }

    }

    @GetMapping(value = "/openingBalanceReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpeningBalanceReportPdf(
            @RequestParam Long balanceMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("balance_m_id", balanceMId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPENING_BALANCE_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.OPENING_BALANCE_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
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
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_TAKING_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/stockTakingReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintStockTakingReportPdf(
            @RequestParam Long takingMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("TAKING_M_ID", takingMId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_TAKING_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_TAKING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/drugExpiryReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintDrugExpiryReportPdf(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam (required = false) Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {
        Long safeItemId = itemId != null ? itemId: 0L;
        Map<String, Object> params = new HashMap<>();
        params.put("HOSPITAL_ID", hospitalId);
        params.put("DEPARTMENT_ID", departmentId);
        params.put("ITEM_ID", safeItemId);
        params.put("FromDate", fromDate);
        params.put("ToDate", toDate);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DRUG_EXPIRY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DRUG_EXPIRY_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DRUG_EXPIRY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintIndentReportPdf(
            @RequestParam Long indentMId ,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("indent_m_id", indentMId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/opdToken", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdTokenPdf(
            @RequestParam Long visit ,
            @RequestParam String flag) {
        Map<String , Object> params = new HashMap<>();
        params.put("visit_id",visit);
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_TOKEN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_TOKEN_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_TOKEN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/labInvestigationReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintLabInvestigationReport(
            @RequestParam Integer orderHdId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("orderhd_id", orderHdId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB,ReportConstants.LAB_INVESTIGATION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.LAB_INVESTIGATION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB,ReportConstants.LAB_INVESTIGATION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/opdCaseSheetReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdCaseSheetReport(
            @RequestParam Long visitId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("visit_id", visitId);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_OPD + ReportConstants.OPD_SUBREPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try {
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_CASESHEET_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_CASESHEET_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_CASESHEET_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/indentMedicineIssueRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintMedicineIssueRegisterReport(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam (required = false) Long itemId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) String indentType,
            @RequestParam String flag ) {
        Long safeItemId = (itemId == null ? 0L : itemId);
        Date safeFromDate = (fromDate != null) ? new Date(fromDate.getTime()) : null;
        Date safeToDate = (toDate != null) ? new Date(toDate.getTime()) : null;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("drug_id", safeItemId);
        params.put("from_date", safeFromDate);
        params.put("to_date", safeToDate);
        params.put("indent_type", indentType);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_STORE + ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_SUBREPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_MEDICINE_ISSUE_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
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
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_ISSUE_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_ISSUE_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_ISSUE_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadIndentReceiving(
            @RequestParam Long receiveMId,
            @RequestParam String flag ) {
        Map<String, Object> params = new HashMap<>();
        params.put("receive_m_id", receiveMId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_RECEIVING_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.INDENT_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/labRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadLabRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam(required = false) Long genderId,
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long fromAge,
            @RequestParam(required = false) Long toAge ,
            @RequestParam String flag) {
        Long safeGenderId = (genderId == null ? 0L : genderId);
        Long safeInvestigationId = (investigationId == null ? 0L : investigationId);
        Long safeFromAge = (fromAge == null ? 0L : fromAge);
        Long safeToAge = (toAge == null ? 0L : toAge);
        Long safeMainChargeCodeId=mainChargecodeId;

        Map<String, Object> params =  new HashMap<>();
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("gender_id", safeGenderId);
        params.put("investigation_id", safeInvestigationId);
        params.put("from_age", safeFromAge);
        params.put("to_age", safeToAge);
        params.put("mainChargeCodeId", safeMainChargeCodeId);

        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_LAB + ReportConstants.LAB_REGISTER_SUB_REPORT_DIR)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.LAB_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.LAB_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/itemWiseReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadItemWiseReceiving(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long itemId,
            @RequestParam String flag) {

        Date safeFromDate = (fromDate != null) ? new Date(fromDate.getTime()) : null;
        Date safeToDate = (toDate != null) ? new Date(toDate.getTime()) : null;

        Map<String, Object> params = new HashMap<>();
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        params.put("hospital_id", hospitalId);
        params.put("deptId", departmentId);
        params.put("from_date", safeFromDate);
        params.put("to_date", safeToDate);
        params.put("item_id", itemId);

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.ITEM_WISE_RECEIVING_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/dateWiseReceiving", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadDateWiseReceiving(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String indentType,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("indent_type", indentType);

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RECEIVING_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DATE_WISE_RECEIVING_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RECEIVING_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/indentReturn", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadIndentReturn(
            @RequestParam Long returnMId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("return_m_id", returnMId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.INDENT_RETURN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.INDENT_RETURN_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.INDENT_RETURN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/itemWiseReturn", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadItemWiseReturn(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long itemId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        Date safeFromDate = (fromDate != null) ? new Date(fromDate.getTime()) : null;
        Date safeToDate = (toDate != null) ? new Date(toDate.getTime()) : null;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("item_id", itemId);
        params.put("from_date", safeFromDate);
        params.put("to_date", safeToDate);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RETURN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.ITEM_WISE_RETURN_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.ITEM_WISE_RETURN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/dateWiseReturn", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadDateWiseReturn(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam String indentType,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("indent_type", indentType);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RETURN_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DATE_WISE_RETURN_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY, ReportConstants.DATE_WISE_RETURN_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/detailTat", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadDetailedTat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long investigationId,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("hospital_id", hospitalId);
        params.put("investigation_id", investigationId);
        params.put("sub_chargecode_id", subChargeCodeId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.DETAILED_TAT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DETAILED_TAT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.DETAILED_TAT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/summaryTat", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadSummaryTat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long investigationId,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("hospital_id", hospitalId);
        params.put("investigation_id", investigationId);
        params.put("sub_chargecode_id", subChargeCodeId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.SUMMARY_TAT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.SUMMARY_TAT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.SUMMARY_TAT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/resultAmendment", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadResultAmendment(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long investigationId,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam (required = false) String patientName,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) String patientMobileNumber,
            @RequestParam String flag) {
        Long safeInvestigationId = (investigationId == null ? 0L : investigationId);
        Long safeSubChargeCodeId = (subChargeCodeId == null ? 0L : subChargeCodeId);
        Timestamp safeFromDate = (fromDate != null) ? new Timestamp(fromDate.getTime()) : null;
        Timestamp safeToDate = (toDate != null) ? new Timestamp(toDate.getTime()) : null;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("investigation_id", safeInvestigationId);
        params.put("sub_chargecode_id", safeSubChargeCodeId);
        params.put("patient_name", patientName);
        params.put("from_date", safeFromDate);
        params.put("to_date", safeToDate);
        params.put("mobile_no", patientMobileNumber);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.RESULT_AMENDMENT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.RESULT_AMENDMENT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.RESULT_AMENDMENT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/stockMovement", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadStockMovement(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId,
            @RequestParam Long itemId,
            @RequestParam String batchNo,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", departmentId);
        params.put("item_id", itemId);
        params.put("batch_no", batchNo);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_MOVEMENT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.STOCK_MOVEMENT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_STORE, ReportConstants.STOCK_MOVEMENT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/radiologyReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadRadioLogy(
            @RequestParam Long radOrderDtId,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("rad_orderdt_id", radOrderDtId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_RADIOLOGY, ReportConstants.RADIOLOGY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.RADIOLOGY_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_RADIOLOGY, ReportConstants.RADIOLOGY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/appointSummaryDept", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadAppointmentSummaryDept(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/appointSummaryDeptDash", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadAppointmentSummaryDeptDashed(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_JASPER_DASHED, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_REPORT_DASHED);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DEPARTMENT_JASPER_DASHED, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/appointSummaryDoctor", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadAppointmentSummaryDoctor(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/appointSummaryDoctorDash", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadAppointmentSummaryDoctorDash(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_JASPER_DASHED, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_REPORT_DASHED);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.APPOINTMENT_SUMMARY_DOCTOR_JASPER_DASHED, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/opdRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadOpdRegister(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long genderId,
            @RequestParam (required = false) Long doctorId,
            @RequestParam (required = false) Long icdId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeGenderId = genderId != null ? genderId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;
        Long safeIcdId = icdId != null ? icdId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("gender_id", safeGenderId);
        params.put("doctor_id", safeDoctorId);
        params.put("icd_id", safeIcdId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_OPD, ReportConstants.OPD_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/dailyCancellation", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewDownloadDailyCancellation(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam (required = false) Long cancellationId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;
        Long safeCancellationId = cancellationId != null ? cancellationId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("cancellation_reason_id", safeCancellationId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.DAILY_CANCELLATION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DAILY_CANCELLATION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_REGISTRATION, ReportConstants.DAILY_CANCELLATION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/radiologyInvoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintRadiologyInvoice(
            @RequestParam String billNo,
            @RequestParam String flag) {
        Map<String, Object> params = new HashMap<>();
        params.put("Bill_no", billNo);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_RADIOLOGY, ReportConstants.RADIOLOGY_INVOICE_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.RADIOLOGY_INVOICE_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_RADIOLOGY, ReportConstants.RADIOLOGY_INVOICE_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/sampleRejection", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintSampleRejection(
            @RequestParam Long hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam String flag) {

        Long safeSubChargeCodeId = subChargeCodeId != null ? subChargeCodeId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("sub_chargecode_id", safeSubChargeCodeId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.SAMPLE_REJECTION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.SAMPLE_REJECTION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.SAMPLE_REJECTION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/pendingInvestigation", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintPendingInvestigation(
            @RequestParam Long hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam String flag) {

        Long safeSubChargeCodeId = subChargeCodeId != null ? subChargeCodeId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("sub_chargecode_id", safeSubChargeCodeId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());
        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.PENDING_INVESTIGATION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.PENDING_INVESTIGATION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_LAB, ReportConstants.PENDING_INVESTIGATION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/opdBillingRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintOpdBillingRegister(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long doctorId,
            @RequestParam String flag) {

        Long safeDepartmentId = departmentId != null ? departmentId: 0L;
        Long safeDoctorId = doctorId != null ? doctorId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("department_id", safeDepartmentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("doctor_id", safeDoctorId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.OPD_BILLING_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.OPD_BILLING_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.OPD_BILLING_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/labBillingRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintLabBillingRegister(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        Long safeSubChargeCodeId = subChargeCodeId != null ? subChargeCodeId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("sub_chargecode_id", safeSubChargeCodeId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.LAB_BILLING_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.LAB_BILLING_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.LAB_BILLING_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/radiologyBillingRegister", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintRadioBillingRegister(
            @RequestParam Long hospitalId,
            @RequestParam (required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        Long safeSubChargeCodeId = subChargeCodeId != null ? subChargeCodeId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("sub_chargecode_id", safeSubChargeCodeId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.RADIOLOGY_BILLING_REGISTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.RADIOLOGY_BILLING_REGISTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.RADIOLOGY_BILLING_REGISTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/dailyCashCollection", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintDailyCashCollection(
            @RequestParam Long hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.DAILY_CASH_COLLECTION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.DAILY_CASH_COLLECTION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.DAILY_CASH_COLLECTION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/cashierWiseCollection", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintCashierWiseCollection(
            @RequestParam Long hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam (required = false) Long cashierId,
            @RequestParam String flag) {

        Long safeCashierId = cashierId != null ? cashierId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("hospital_id", hospitalId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("cashier_id", safeCashierId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.CASHIER_WISE_COLLECTION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.CASHIER_WISE_COLLECTION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BILLING, ReportConstants.CASHIER_WISE_COLLECTION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/bloodInventoryStockSummary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintBloodInventoryStockSummary(
            @RequestParam (required = false) String bloodGroupId,
            @RequestParam (required = false) Long componentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        List<Long> bloodGroupIds = null;

        if (bloodGroupId != null && !bloodGroupId.trim().isEmpty()) {
            bloodGroupIds = Arrays.stream(bloodGroupId.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // Optional: handle empty result after filtering
            if (bloodGroupIds.isEmpty()) {
                bloodGroupIds = null;
            }
        }

        Long safeComponentId = componentId != null ? componentId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("blood_group_id", bloodGroupIds);
        params.put("component_id", safeComponentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BLOOD_BANK, ReportConstants.BLOOD_INVENTORY_STOCK_SUMMARY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.BLOOD_INVENTORY_STOCK_SUMMARY_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BLOOD_BANK, ReportConstants.BLOOD_INVENTORY_STOCK_SUMMARY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/bloodInventoryStockDetail", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintBloodInventoryStockDetail(
            @RequestParam (required = false) String bloodGroupId,
            @RequestParam (required = false) Long componentId,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam String flag) {

        List<Long> bloodGroupIds = null;

        if (bloodGroupId != null && !bloodGroupId.trim().isEmpty()) {
            bloodGroupIds = Arrays.stream(bloodGroupId.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // Optional: handle empty result after filtering
            if (bloodGroupIds.isEmpty()) {
                bloodGroupIds = null;
            }
        }

        Long safeComponentId = componentId != null ? componentId: 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("blood_group_id", bloodGroupIds);
        params.put("component_id", safeComponentId);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)) {
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_BLOOD_BANK, ReportConstants.BLOOD_INVENTORY_STOCK_DETAIL_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf, ReportConstants.BLOOD_INVENTORY_STOCK_DETAIL_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)) {
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_BLOOD_BANK, ReportConstants.BLOOD_INVENTORY_STOCK_DETAIL_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/ipDailyCaseSheet", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintIpDailyCaseSheet(
            @RequestParam Integer inPatientId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("inPatientId", inPatientId);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_IPD + ReportConstants.IPD_SUB_REPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_DAILY_CASE_SHEET_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.IP_DAILY_CASE_SHEET_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_DAILY_CASE_SHEET_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/ipVitalsReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintIpVitalsReport(
            @RequestParam Integer inPatientId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("inPatientId", inPatientId);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_IPD + ReportConstants.IPD_SUB_REPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_VITALS_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.IP_VITALS_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_VITALS_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/ipInvestigationReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintIpInvestigationReport(
            @RequestParam Integer inPatientId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("inPatientId", inPatientId);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_IPD + ReportConstants.IPD_SUB_REPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_INVESTIGATION_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.IP_INVESTIGATION_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.IP_INVESTIGATION_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/dischageSummary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintDischargeSummary(
            @RequestParam Integer inPatientId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("inPatientId", inPatientId);
        params.put("SUBREPORT_DIR", Objects.requireNonNull(getClass().getResource(ReportConstants.JASPER_BASE_PATH_IPD + ReportConstants.IPD_SUB_REPORT_DIR)).toString());
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.DISCHARGE_SUMMARY_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.DISCHARGE_SUMMARY_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.DISCHARGE_SUMMARY_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/advanceReceipt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintAdvanceReceipt(
            @RequestParam Integer receiptId,
            @RequestParam String flag){
        Map<String , Object> params = new HashMap<>();
        params.put("receiptId", receiptId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.ADVANCE_RECEIPT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.ADVANCE_RECEIPT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.ADVANCE_RECEIPT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/drugMaster", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintDrugMaster(
            @RequestParam Integer sectionId,
            @RequestParam String flag){
        Long safeSectionId = sectionId != null ? sectionId: 0L;
        Map<String , Object> params = new HashMap<>();
        params.put("sectionId", safeSectionId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_DISPENSARY,ReportConstants.DRUG_MASTER_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.DRUG_MASTER_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_DISPENSARY,ReportConstants.DRUG_MASTER_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
        }
    }

    @GetMapping(value = "/medicalNonMedicalConsumable", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewPrintAdvanceReceipt(
            @RequestParam Integer itemTypeId,
            @RequestParam Integer sectionId,
            @RequestParam String flag){
        Long safeSectionId = sectionId != null ? sectionId: 0L;
        Map<String , Object> params = new HashMap<>();
        params.put("receiptId", itemTypeId);
        params.put("sectionId", safeSectionId);
        params.put("path", Objects.requireNonNull(getClass().getResource(ReportConstants.ASSET_LOGO)).toString());

        try{
            if (ReportConstants.REPORT_FLAG_DOWNLOAD.equalsIgnoreCase(flag)){
                byte[] viewPdf = JasperReportUtil.generateAndViewPdfReport(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.ADVANCE_RECEIPT_JASPER, params, getConnection());
                return buildPdfResponse(viewPdf,ReportConstants.ADVANCE_RECEIPT_REPORT);
            } else if (ReportConstants.REPORT_FLAG_PRINT.equalsIgnoreCase(flag)){
                JasperPrint jasperPrint = JasperReportUtil.getJasperPrintObject(ReportConstants.JASPER_BASE_PATH_IPD,ReportConstants.ADVANCE_RECEIPT_JASPER, params, getConnection());
                JasperReportUtil.printJasperReport(jasperPrint);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createNotFoundResponse(
                                ReportConstants.ERROR_INVALID_FLAG, ReportConstants.HTTP_STATUS_BAD_REQUEST));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReportConstants.ERROR_FAILED_TO_GENERATE_REPORT + e.getMessage());
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


