package com.hims.service.impl;

import com.hims.entity.MasDepartment;
import com.hims.entity.MasHospital;
import com.hims.entity.StoreItemBatchStock;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.StoreItemBatchStockRepository;
import com.hims.service.StockStatusReportService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockStatusReportServiceImpl implements StockStatusReportService {

    private static final Logger log = LoggerFactory.getLogger(StockStatusReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StoreItemBatchStockRepository sibsRepo;

    @Autowired
    private MasHospitalRepository hospitalRepo;

    @Autowired
    private MasDepartmentRepository deptRepo;

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
    public ResponseEntity<byte[]> generateStockSummaryReport(Long hospitalId, Long departmentId, String path) {
       try{
           if(hospitalId == null ||departmentId == null){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required parameter: visit_id".getBytes());}

           MasHospital hospital = hospitalRepo.findById(hospitalId)
                   .orElseThrow(() -> new IllegalArgumentException("Invalid HOSPITAL_ID: " + hospitalId));

           MasDepartment department = deptRepo.findById(departmentId)
                   .orElseThrow(() -> new IllegalArgumentException("Invalid DEPARTMENT_ID: " + departmentId));

           List<StoreItemBatchStock> stockList = sibsRepo.findByhospitalIdAndDepartmentId(hospital, department);

           if (stockList.isEmpty()) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(("No matching billing record found for HOSPITAL_ID: " + hospitalId + "and DEPARTMENT_ID: " + departmentId).getBytes());
           }

           StoreItemBatchStock itemBatchStock = stockList.get(0);

           SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
           String formattedDate = sdf.format(new Date());

           Map<String, Object> parameters = new HashMap<>();
           parameters.put("HOSPITAL_ID", itemBatchStock.getHospitalId().getId());
           parameters.put("DEPARTMENT_ID", itemBatchStock.getDepartmentId().getId());
           parameters.put("CurrentDate", formattedDate);
           parameters.put("path", path);

           Connection conn = dataSource.getConnection();
           byte[] data = reportDeclare("Stock_Status_Summary", parameters, conn);

           HttpHeaders headers = new HttpHeaders();
           headers.setContentType(MediaType.APPLICATION_PDF);
           headers.setContentDisposition(ContentDisposition.builder("inline")
                   .filename("Stock_Status_Summary.pdf")
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

    @Override
    public ResponseEntity<byte[]> generateStockDetailedReport(Long hospitalId, Long departmentId, String path) {
        try{
            if(hospitalId == null ||departmentId == null){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required parameter: visit_id".getBytes());}

            MasHospital hospital = hospitalRepo.findById(hospitalId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid HOSPITAL_ID: " + hospitalId));

            MasDepartment department = deptRepo.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid DEPARTMENT_ID: " + departmentId));

            List<StoreItemBatchStock> stockList = sibsRepo.findByhospitalIdAndDepartmentId(hospital, department);

            if (stockList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("No matching billing record found for HOSPITAL_ID: " + hospitalId + "and DEPARTMENT_ID: " + departmentId).getBytes());
            }

            StoreItemBatchStock itemBatchStock = stockList.get(0);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = sdf.format(new Date());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("HOSPITAL_ID", itemBatchStock.getHospitalId().getId());
            parameters.put("DEPARTMENT_ID", itemBatchStock.getDepartmentId().getId());
            parameters.put("CurrentDate", formattedDate);
            parameters.put("path", path);

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("Stock_Status_Detail", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Stock_Status_Detail.pdf")
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
