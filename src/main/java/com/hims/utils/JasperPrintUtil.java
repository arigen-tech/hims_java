package com.hims.utils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import org.springframework.stereotype.Component;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

@Component
public class JasperPrintUtil {
    public void print(JasperPrint jasperPrint) {

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
}

