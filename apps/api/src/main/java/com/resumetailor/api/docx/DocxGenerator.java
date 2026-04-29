package com.resumetailor.api.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DocxGenerator {

    @Value("${pdf.skip:false}")
    private boolean skipPdf;

    public byte[] generatePdf(String resumeText) throws Exception {
        byte[] docxBytes = buildDocx(resumeText);
        if (skipPdf) return placeholderPdfBytes();
        return convertToPdf(docxBytes);
    }

    private byte[] buildDocx(String resumeText) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
            CTPageMar pageMar = sectPr.addNewPgMar();
            pageMar.setTop(BigInteger.valueOf(720));
            pageMar.setBottom(BigInteger.valueOf(720));
            pageMar.setLeft(BigInteger.valueOf(1080));
            pageMar.setRight(BigInteger.valueOf(1080));

            for (String line : resumeText.split("\n", -1)) {
                XWPFParagraph para = doc.createParagraph();
                XWPFRun run = para.createRun();
                run.setFontFamily("Calibri");
                run.setFontSize(11);
                run.setText(line);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] convertToPdf(byte[] docxBytes) throws Exception {
        Path tempDir = Files.createTempDirectory("resume-taylor-");
        Path docxPath = tempDir.resolve("output.docx");
        Files.write(docxPath, docxBytes);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "libreoffice", "--headless", "--convert-to", "pdf",
                    "--outdir", tempDir.toString(), docxPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("LibreOffice timed out after 60s");
            }
            if (process.exitValue() != 0) {
                String output = new String(process.getInputStream().readAllBytes());
                throw new RuntimeException("LibreOffice conversion failed: " + output);
            }

            return Files.readAllBytes(tempDir.resolve("output.pdf"));
        } finally {
            Files.deleteIfExists(docxPath);
            Files.deleteIfExists(tempDir.resolve("output.pdf"));
            Files.deleteIfExists(tempDir);
        }
    }

    private byte[] placeholderPdfBytes() {
        String stub = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj " +
                      "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj " +
                      "3 0 obj<</Type/Page/MediaBox[0 0 612 792]>>endobj\n" +
                      "xref\n0 4\ntrailer<</Root 1 0 R/Size 4>>\nstartxref\n0\n%%EOF";
        return stub.getBytes(StandardCharsets.UTF_8);
    }
}
