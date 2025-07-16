package com.hims.service.impl;

import com.hims.entity.BillingHeader;
import com.hims.entity.repository.BillingHeaderRepository;
import com.hims.service.LabReportService;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Service
public class LabReportServiceImpl implements LabReportService {

    private static final Logger log = LoggerFactory.getLogger(LabReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BillingHeaderRepository billingRepo;


    @Override
    public byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception {
        InputStream reportStream = getClass().getResourceAsStream("/jasperReport/" + reportName + ".jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found: " + reportName);
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public ResponseEntity<byte[]> generateLabReport(String billNo) {
        try {
            if (billNo == null || billNo.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing required parameter: Bill_no".getBytes());
            }

            BillingHeader billingHeader = billingRepo.findByBillNo(billNo);

            if (billingHeader == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("No billing record found for Bill_no: " + billNo).getBytes());
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Bill_no", billingHeader.getBillNo());

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("Lab_report", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Lab_report.pdf")
                    .build());

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (FileNotFoundException fnfe) {
            log.error("JRXML not found: {}", fnfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Report template missing: " + fnfe.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Report generation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Report generation error: " + e.getMessage()).getBytes());
        }
    }
}

