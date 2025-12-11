package com.hims.service.impl;

import com.hims.entity.DgOrderHd;
import com.hims.entity.repository.LabHdRepository;
import com.hims.service.LabInvestigationResultReportService;
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

@Service
public class LabInvestigationReportServiceImpl implements LabInvestigationResultReportService {
    private static final Logger log = LoggerFactory.getLogger(LabInvestigationReportServiceImpl.class);

    @Autowired
    private LabHdRepository labRepo;

    @Autowired
    private DataSource dataSource;

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
    public ResponseEntity<byte[]> generateLabInvestigationResultReport(Long orderHdId) {
        try{
            if (orderHdId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing required parameter: orderhd_id".getBytes());
            }

            DgOrderHd dgOrderHd = labRepo.findById(orderHdId);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("orderhd_id", dgOrderHd.getId());
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
