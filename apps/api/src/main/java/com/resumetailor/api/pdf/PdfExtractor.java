package com.resumetailor.api.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfExtractor {

    public String extract(byte[] pdfBytes) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            if (doc.isEncrypted()) {
                throw new PdfExtractionException(
                        "Couldn't read this PDF — it may be password-protected. " +
                        "Try exporting it as a new PDF and re-uploading.");
            }
            String text = new PDFTextStripper().getText(doc).strip();
            if (text.isBlank()) {
                throw new PdfExtractionException(
                        "Couldn't extract text from this PDF — it may be a scanned " +
                        "image. Try copy-pasting your resume as plain text instead.");
            }
            return text;
        } catch (IOException e) {
            throw new PdfExtractionException(
                    "Couldn't read this PDF — it may be corrupt. " +
                    "Try saving it as a new PDF and re-uploading.");
        }
    }
}
