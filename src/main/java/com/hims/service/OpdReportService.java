package com.hims.service;
import com.hims.entity.Visit;
import org.springframework.http.ResponseEntity;
import java.sql.Connection;
import java.util.Map;

public interface OpdReportService {
    byte[] reportDeclare(String reportName, Map<String, Object> parameters, Connection conn) throws Exception;
    ResponseEntity<byte[]> generateOpdReport(Long visit);
}
