package com.hims.service.impl;

import com.hims.service.CommonPrintService;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

@Service
public class CommonPrintServiceImpl implements CommonPrintService {

    @Override
    public JasperPrint printJasperDirectly(String basePath, String reportName, Map<String, Object> params, Connection conn) throws Exception {
        InputStream report = getClass().getResourceAsStream(basePath + reportName);
        if (report == null) {
            throw new RuntimeException("Jasper report not found: " + basePath + reportName);
        }
        return JasperFillManager.fillReport(report, params, conn);
    }
}
