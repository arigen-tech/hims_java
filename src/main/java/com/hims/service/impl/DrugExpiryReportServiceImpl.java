package com.hims.service.impl;

import com.hims.entity.MasDepartment;
import com.hims.entity.MasHospital;
import com.hims.entity.MasStoreItem;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasStoreItemRepository;
import com.hims.service.DrugExpiryReportService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DrugExpiryReportServiceImpl implements DrugExpiryReportService {

    private static final Logger log = LoggerFactory.getLogger(DrugExpiryReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MasHospitalRepository hospitalRepo;

    @Autowired
    private MasDepartmentRepository deptRepo;

    @Autowired
    private MasStoreItemRepository itemRepo;

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
    public ResponseEntity<byte[]> generateDrugExpiryReport(Long hospitalId, Long departmentId, Long itemId, Date fromDate, Date toDate) {
        try {
            MasHospital hospital = (hospitalId != null) ? hospitalRepo.findById(hospitalId).orElse(null) : null;
            MasDepartment department = (departmentId != null) ? deptRepo.findById(departmentId).orElse(null) : null;
            MasStoreItem item = (itemId != null) ? itemRepo.findById(itemId).orElse(null) : null;

            Long safeHospitalId = (hospital != null) ? hospital.getId() : (hospitalId != null ? hospitalId : 0L);
            Long safeDepartmentId = (department != null) ? department.getId() : (departmentId != null ? departmentId : 0L);
            Long safeItemId = (item != null) ? item.getItemId() : (itemId != null ? itemId : 0L);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("HOSPITAL_ID", safeHospitalId);
            parameters.put("DEPARTMENT_ID", safeDepartmentId);
            parameters.put("ITEM_ID", safeItemId);
            parameters.put("FromDate", fromDate);
            parameters.put("ToDate", toDate);
            parameters.put("path", getClass()
                    .getResource("/Assets/arigen_health.png")
                    .toString());

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("Drug_Expiry_Report", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Drug_Expiry_Report.pdf")
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
