package com.hims.service;

import net.sf.jasperreports.engine.JasperPrint;

import java.sql.Connection;
import java.util.Map;

public interface CommonPrintService {
    JasperPrint printJasperDirectly(String basePath, String reportName, Map<String, Object> params, Connection conn) throws Exception;

}
