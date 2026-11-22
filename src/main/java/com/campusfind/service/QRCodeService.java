package com.campusfind.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class QRCodeService {

    public String generateUniqueCode(String itemId) {
        if (itemId != null && !itemId.isBlank()) {
            return "QR-" + itemId.trim().toUpperCase();
        }
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BufferedImage generateQRImage(String code, int width, int height) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Kode QR tidak boleh kosong.");
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(code.trim(), BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("Gagal membuat gambar QR Code: " + e.getMessage(), e);
        }
    }

    public File saveQRToPng(String code, File outputFile, int size) throws IOException {
        BufferedImage image = generateQRImage(code, size, size);
        File target = outputFile.getName().toLowerCase().endsWith(".png")
                ? outputFile
                : new File(outputFile.getAbsolutePath() + ".png");
        ImageIO.write(image, "png", target);
        return target;
    }

    public String decodeQRFromFile(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("File gambar QR tidak ditemukan.");
        }
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Berkas yang dipilih bukan gambar yang valid.");
        }
        try {
            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))
            );
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("QR Code tidak terbaca pada gambar tersebut.", e);
        }
    }
}