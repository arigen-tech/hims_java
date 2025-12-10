package com.hims.service.impl;

import com.hims.entity.BillingHeader;
import com.hims.entity.Visit;
import com.hims.entity.repository.BillingHeaderRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.service.OpdReportService;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OpdReportServiceImpl implements OpdReportService {

    private static final Logger log = LoggerFactory.getLogger(OpdReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BillingHeaderRepository billingRepo;

    @Autowired
    private VisitRepository visitRepo;

    private String path = "src/main/resources/Assets/arigen_health.png";

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
    public ResponseEntity<byte[]>  generateOpdReport(Long visit) {
        try {
            if (visit == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing required parameter: visit_id".getBytes());
            }

            Visit visitEntity = visitRepo.findById(visit)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid visit_id: " + visit));

            BillingHeader billingHeader = billingRepo.findByVisit(visitEntity);

            if (billingHeader == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("No matching billing record found for visit_id: " + visit).getBytes());
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("visit_id", billingHeader.getVisit().getId());
            parameters.put("path", path);

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("opd_billing_maxx", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("opd_billing.pdf")
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
