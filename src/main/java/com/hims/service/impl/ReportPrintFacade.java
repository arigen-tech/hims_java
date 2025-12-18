package com.hims.service.impl;

import com.hims.service.CommonPrintService;
import com.hims.utils.JasperPrintUtil;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Map;

@Service
public class ReportPrintFacade {

    @Autowired
    private CommonPrintService commonPrintService;

    @Autowired
    private JasperPrintUtil printUtil;

    public void printJasperReport(String reportName, Map<String, Object> params, Connection conn) throws Exception {
        JasperPrint jasperPrint = commonPrintService.printJasperDirectly("/jasperReport/", reportName, params, conn);
        printUtil.print(jasperPrint);
    }

}
