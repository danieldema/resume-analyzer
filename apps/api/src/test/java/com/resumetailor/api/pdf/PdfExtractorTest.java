package com.resumetailor.api.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfExtractorTest {

    private final PdfExtractor extractor = new PdfExtractor();

    @Test
    void extract_returnsText() throws Exception {
        byte[] pdf = createPdfWithText("Hello World");
        assertThat(extractor.extract(pdf)).contains("Hello World");
    }

    @Test
    void extract_blankPdf_throwsPdfExtractionException() throws Exception {
        byte[] pdf = createBlankPdf();
        assertThatThrownBy(() -> extractor.extract(pdf))
                .isInstanceOf(PdfExtractionException.class);
    }

    @Test
    void extract_corruptBytes_throwsPdfExtractionException() {
        byte[] notAPdf = "this is not a pdf".getBytes();
        assertThatThrownBy(() -> extractor.extract(notAPdf))
                .isInstanceOf(PdfExtractionException.class);
    }

    private byte[] createPdfWithText(String text) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(100, 700);
                stream.showText(text);
                stream.endText();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.save(bos);
            return bos.toByteArray();
        }
    }

    private byte[] createBlankPdf() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.save(bos);
            return bos.toByteArray();
        }
    }
}
