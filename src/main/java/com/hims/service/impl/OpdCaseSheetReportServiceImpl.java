package com.hims.service.impl;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.entity.repository.OpdPatientDetailRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.service.OpdCaseSheetService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpdCaseSheetReportServiceImpl implements OpdCaseSheetService {
    private static final Logger log = LoggerFactory.getLogger(OpdCaseSheetReportServiceImpl.class);

    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepo;

    @Autowired
    private VisitRepository visitRepo;

    @Autowired
    private DataSource dataSource;

    @Override
    public byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception {
        InputStream reportStream = getClass().getResourceAsStream("/jasperReport/opdCaseSheetReport/" + reportName + ".jasper");
        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found: " + reportName);
        }
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public ResponseEntity<byte[]> generateOpdCaseSheetReport(Long visitId) {
        try{
            if (visitId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing required parameter: visit_id".getBytes());
            }

            OpdPatientDetail opdPatientDetail = opdPatientDetailRepo.findByVisitId(visitId);

            if (opdPatientDetail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("No matching opd record found for visit_id: " + visitId).getBytes());
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SUBREPORT_DIR", getClass()
                    .getResource("/jasperReport/opdCaseSheetReport/")
                    .toString());
            parameters.put("visit_id", opdPatientDetail.getVisit().getId());
            parameters.put("path", getClass()
                    .getResource("/Assets/arigen_health.png")
                    .toString());

            Connection conn = dataSource.getConnection();
            byte[] data = reportDeclare("opd_casesheet_new_report_1", parameters, conn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("opd_casesheet_new_report_1.pdf")
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
    public JasperPrint printOpdCaseSheetReport(Long visitId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("SUBREPORT_DIR", getClass()
                .getResource("/jasperReport/opdCaseSheetReport/")
                .toString());
        params.put("visit_id", visitId);
        params.put("path", getClass()
                .getResource("/Assets/arigen_health.png")
                .toString());

        InputStream report =
                getClass().getResourceAsStream("/jasperReport/opdCaseSheetReport/opd_casesheet_new_report_1.jasper");



        try (Connection conn = dataSource.getConnection()) {
//            return JasperFillManager.fillReport(report, params, conn);
            JasperPrint jasperPrint =
                    JasperFillManager.fillReport(
                            report,
                            params,
                            conn
                    );

            // for dihhbugging purposes only
            String dir =
                    System.getProperty("user.home")
                            + File.separator
                            + "Downloads"
                            + File.separator
                            + "hims-reports"
                            + File.separator;

            Files.createDirectories(Paths.get(dir));

            String pdfPath = dir + "opd-case-sheet.pdf";

            JasperExportManager.exportReportToPdfFile(
                    jasperPrint,
                    pdfPath
            );

            return jasperPrint;
        }
    }
}
