package com.hims.service;

import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

public interface StockTakingRegisterReportService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateStockTakingRegistry(Long hospitalId, Long departmentId, Date fromDate, Date toDate);
}
