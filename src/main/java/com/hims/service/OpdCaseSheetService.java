package com.hims.service;

import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.util.Map;

public interface OpdCaseSheetService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateOpdCaseSheetReport(Long visitId);
    JasperPrint printOpdCaseSheetReport(Long visitId) throws Exception;
}
