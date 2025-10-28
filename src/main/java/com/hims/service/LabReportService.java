package com.hims.service;

import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.util.Map;

public interface LabReportService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateLabReport(String billNo, String paymentStatus);
}
