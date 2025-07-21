package com.hims.service;

import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.util.Map;

public interface StockStatusReportService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateStockSummaryReport(Long hospitalId, Long departmentId, String path);
    ResponseEntity<byte[]> generateStockDetailedReport(Long hospitalId, Long departmentId, String path);
}
