package com.vodokanal.accounting.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.vodokanal.accounting.dto.BillCalculationDTO;
import com.vodokanal.accounting.dto.BillMeterDto;
import com.vodokanal.accounting.util.QRCodeGenerator;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocumentService {
    TemplateEngine templateEngine;
    QRCodeGenerator qrCodeGenerator;

    public DocumentService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.qrCodeGenerator = new QRCodeGenerator();
    }

    public byte[] createBill(
            List<BillCalculationDTO> billCalculationDTOList,
            List<BillMeterDto> billMeterDTOList,
            LocalDate billPeriod) {
        Context context = new Context();

        String qrCodeBase64 = qrCodeGenerator.generateQRCode("lorem ipsum");
        context.setVariable("qrCode", "data:image/png;base64," + qrCodeBase64);

        DateTimeFormatter formatString = DateTimeFormatter.ofPattern("LLLL yyyy");
        DateTimeFormatter formatPeriod = DateTimeFormatter.ofPattern("MM yy");
        context.setVariable("payMonthString", billPeriod.format(formatString));
        context.setVariable("payMonthPeriod", billPeriod.format(formatPeriod));

        context.setVariable("accountNumber", billCalculationDTOList.getFirst().account());
        context.setVariable("payer",  billCalculationDTOList.getFirst().payer());
        context.setVariable("address",  billCalculationDTOList.getFirst().address());
        context.setVariable("residentNumber", billCalculationDTOList.getFirst().residents());

        context.setVariable("meters", billMeterDTOList);
        context.setVariable("services", billCalculationDTOList);

        BigDecimal total = billCalculationDTOList
                .stream()
                .map(BillCalculationDTO::sum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setVariable("total", total);
        context.setVariable("payBefore", billPeriod.plusDays(15));

        String htmlContent = templateEngine.process("bill", context);
        ByteArrayOutputStream pdfFile = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, pdfFile);

        return pdfFile.toByteArray();
    }


}
