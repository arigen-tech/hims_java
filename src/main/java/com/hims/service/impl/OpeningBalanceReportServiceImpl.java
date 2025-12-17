package com.hims.service.impl;

import com.hims.entity.StoreBalanceHd;
import com.hims.entity.repository.StoreBalanceHdRepository;
import com.hims.service.OpeningBalanceReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
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
public class OpeningBalanceReportServiceImpl implements OpeningBalanceReportService {

    private static final Logger log = LoggerFactory.getLogger(OpeningBalanceReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StoreBalanceHdRepository stBalMRepo;


    @Override
    public byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception {
        InputStream reportStream = getClass().getResourceAsStream("/jasperReport/" + reportName + ".jasper");
        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found: " + reportName);
        }
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public ResponseEntity<byte[]> generateOpeningBalanceReport(Long balanceMId){
        try {
            StoreBalanceHd storeHd = (balanceMId != null) ? stBalMRepo.findById(balanceMId).orElse(null) : null;
            Long safeBalanceMId = (storeHd != null) ? storeHd.getBalanceMId() : (balanceMId != null ? balanceMId : 0L);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("balance_m_id", safeBalanceMId);
            parameters.put("path", getClass()
                    .getResource("/Assets/arigen_health.png")
                    .toString());

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("Opening_Balance_report", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Opening_Balance_report.pdf")
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
