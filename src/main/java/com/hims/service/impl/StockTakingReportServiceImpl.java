package com.hims.service.impl;

import com.hims.entity.MasHospital;
import com.hims.entity.StoreStockTakingM;
import com.hims.entity.StoreStockTakingT;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.StoreStockTakingMRepository;
import com.hims.entity.repository.StoreStockTakingTRepository;
import com.hims.service.StockTakingReportService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockTakingReportServiceImpl implements StockTakingReportService {

    private static final Logger log = LoggerFactory.getLogger(StockTakingReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MasHospitalRepository hospitalRepo;

    @Autowired
    private StoreStockTakingMRepository storeStockTakingMRepo;

    @Autowired
    private StoreStockTakingTRepository storeStockTakingTRepo;

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
    public ResponseEntity<byte[]> generateStockTaking(Long takingMId) {
        try{
            StoreStockTakingM takingM = (takingMId != null) ? storeStockTakingMRepo.findById(takingMId).orElse(null) : null;
            List<StoreStockTakingT> takingTList = (takingM != null) ? storeStockTakingTRepo.findByTakingMId(takingM) : Collections.emptyList();

            Long safeTakingMId = (takingM != null) ? takingM.getTakingMId() : (takingMId != null ? takingMId : 0l);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TAKING_M_ID", safeTakingMId);
            parameters.put("path", getClass()
                    .getResource("/Assets/arigen_health.png")
                    .toString());

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("Stock_taking_report", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Stock_taking_report.pdf")
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
