package com.hims.service;

import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.util.Map;

public interface StockTakingReportService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateStockTaking(Long hospitalId, Long takingMId);
}
