package com.vodokanal.accounting.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QRCodeGenerator {

    public String generateQRCode (String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        byte[] pngBytes;

        try(ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 100, 100);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            pngBytes = pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(pngBytes);
    }
}
