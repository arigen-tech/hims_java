package com.hims.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

public class JasperReportUtil {

    private JasperReportUtil() {
    }

    public static byte[] generateAndViewPdfReport(
            String basePath,
            String jasperName,
            Map<String, Object> parameters,
            Connection connection
    ) throws Exception {

        InputStream reportStream =
                JasperReportUtil.class.getResourceAsStream(
                        basePath + jasperName + ".jasper");

        if (reportStream == null) {
            throw new FileNotFoundException("Jasper file not found: " + jasperName);
        }

        JasperReport jasperReport =
                (JasperReport) JRLoader.loadObject(reportStream);

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(jasperReport, parameters, connection);

        byte[] pdfData =
                JasperExportManager.exportReportToPdf(jasperPrint);

        return pdfData;
    }

    public static void printJasperReport(JasperPrint jasperPrint) {

        PrintService printService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            throw new RuntimeException("No default printer found");
        }

        PrintRequestAttributeSet printAttributes =
                new HashPrintRequestAttributeSet();

        printAttributes.add(new Copies(1));
        printAttributes.add(MediaSizeName.ISO_A4);
        printAttributes.add(OrientationRequested.PORTRAIT);

        JRPrintServiceExporter exporter =
                new JRPrintServiceExporter();

        exporter.setExporterInput(
                new SimpleExporterInput(jasperPrint)
        );

        SimplePrintServiceExporterConfiguration config =
                new SimplePrintServiceExporterConfiguration();

        config.setPrintService(printService);
        config.setPrintRequestAttributeSet(printAttributes);
        config.setDisplayPageDialog(false);
        config.setDisplayPrintDialog(false);

        exporter.setConfiguration(config);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public static JasperPrint getJasperPrintObject(String basePath, String reportName, Map<String, Object> params, Connection conn) throws Exception {
        InputStream report = JasperReportUtil.class.getResourceAsStream(basePath + reportName + ".jasper");
        if (report == null) {
            throw new RuntimeException("Jasper report not found: " + basePath + reportName);
        }
        return JasperFillManager.fillReport(report, params, conn);
    }

    public static ResponseEntity<byte[]> generateExcelReport(
            String jasperName,
            String outputFileName,
            Map<String, Object> parameters,
            Connection connection
    ) throws Exception {

        // Do EXCEL implementation here later
        return null;
    }
}
