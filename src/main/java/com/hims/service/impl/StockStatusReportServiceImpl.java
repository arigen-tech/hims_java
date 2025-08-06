package com.hims.service.impl;

import com.hims.entity.*;
import com.hims.entity.repository.*;
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
import java.util.*;

@Service
public class StockStatusReportServiceImpl implements StockStatusReportService {

    private static final Logger log = LoggerFactory.getLogger(StockStatusReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MasHospitalRepository hospitalRepo;

    @Autowired
    private MasDepartmentRepository deptRepo;

    @Autowired
    private MasItemClassRepository itemClassRepo;

    @Autowired
    private MasStoreSectionRepository sectionRepo;

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
    public ResponseEntity<byte[]> generateStockSummaryReport(Long hospitalId, Long departmentId, Integer itemClassId, Integer sectionId) {
       try{
           MasHospital hospital = (hospitalId != null) ? hospitalRepo.findById(hospitalId).orElse(null) : null;
           MasDepartment department = (departmentId != null) ? deptRepo.findById(departmentId).orElse(null) : null;
           MasItemClass itemClass = (itemClassId != null) ? itemClassRepo.findById(itemClassId).orElse(null) : null;
           MasStoreSection storeSection = (sectionId != null) ? sectionRepo.findById(sectionId).orElse(null) : null;

           Long safeHospitalId = (hospital != null) ? hospital.getId() : (hospitalId != null ? hospitalId : 0L);
           Long safeDepartmentId = (department != null) ? department.getId() : (departmentId != null ? departmentId : 0L);
           Integer safeItemClassId = Math.toIntExact((itemClass != null) ? itemClass.getItemClassId() : (itemClassId != null ? itemClassId : 0L));
           Integer safeSectionId = Math.toIntExact((storeSection != null) ? storeSection.getSectionId() : (sectionId != null ? sectionId : 0L));

           Map<String, Object> parameters = new HashMap<>();
           parameters.put("HOSPITAL_ID", safeHospitalId);
           parameters.put("DEPARTMENT_ID", safeDepartmentId);
           parameters.put("ITEM_CLASS_ID", safeItemClassId);
           parameters.put("SECTION_ID", safeSectionId);
           parameters.put("CurrentDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
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
    public ResponseEntity<byte[]> generateStockDetailedReport(Long hospitalId, Long departmentId, Integer itemClassId, Integer sectionId) {
        try{
            MasHospital hospital = (hospitalId != null) ? hospitalRepo.findById(hospitalId).orElse(null) : null;
            MasDepartment department = (departmentId != null) ? deptRepo.findById(departmentId).orElse(null) : null;
            MasItemClass itemClass = (itemClassId != null) ? itemClassRepo.findById(itemClassId).orElse(null) : null;
            MasStoreSection storeSection = (sectionId != null) ? sectionRepo.findById(sectionId).orElse(null) : null;

            Long safeHospitalId = (hospital != null) ? hospital.getId() : (hospitalId != null ? hospitalId : 0L);
            Long safeDepartmentId = (department != null) ? department.getId() : (departmentId != null ? departmentId : 0L);
            Integer safeItemClassId = Math.toIntExact((itemClass != null) ? itemClass.getItemClassId() : (itemClassId != null ? itemClassId : 0L));
            Integer safeSectionId = Math.toIntExact((storeSection != null) ? storeSection.getSectionId() : (sectionId != null ? sectionId : 0L));

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("HOSPITAL_ID", safeHospitalId);
            parameters.put("DEPARTMENT_ID", safeDepartmentId);
            parameters.put("ITEM_CLASS_ID", safeItemClassId);
            parameters.put("SECTION_ID", safeSectionId);
            parameters.put("CurrentDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
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
