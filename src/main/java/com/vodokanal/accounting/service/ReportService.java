package com.vodokanal.accounting.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {
    public void generatePdf() {
        // Загрузка шаблона из ресурсов
        JasperReport jasperReport = null;
        try {
            jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/bill.jrxml");
        } catch (JRException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("billDateString", "ФЕВРАЛЬ 2026г.");
        parameters.put("billDateNumb", "(02.26)");
        JasperPrint jasperPrint = null;
        try {
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }

        // 4. Экспорт в PDF
        // jasperPrint — это уже заполненный данными отчет
        String outputPath = "D:\\works\\java\\reports\\my_report.pdf";

        try {
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }
}

