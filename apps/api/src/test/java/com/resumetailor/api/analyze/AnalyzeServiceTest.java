package com.resumetailor.api.analyze;

import com.resumetailor.api.gemini.GeminiClient;
import com.resumetailor.api.gemini.SkillsGapResult;
import com.resumetailor.api.pdf.PdfExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AnalyzeServiceTest {

    private PdfExtractor pdfExtractor;
    private GeminiClient geminiClient;
    private AnalyzeService analyzeService;

    private MultipartFile validFile;

    @BeforeEach
    void setUp() throws Exception {
        pdfExtractor = mock(PdfExtractor.class);
        geminiClient = mock(GeminiClient.class);
        analyzeService = new AnalyzeService(pdfExtractor, geminiClient);

        validFile = mock(MultipartFile.class);
        when(validFile.isEmpty()).thenReturn(false);
        when(validFile.getContentType()).thenReturn("application/pdf");
        when(validFile.getSize()).thenReturn(1024L);
        when(validFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    void analyze_happyPath_returnsResponse() {
        when(pdfExtractor.extract(any())).thenReturn("resume text");
        when(geminiClient.analyzeSkillsGap(anyString(), anyString())).thenReturn(
                new SkillsGapResult(80, List.of("Java"), List.of("AWS"), List.of("Kubernetes"))
        );

        AnalyzeResponse response = analyzeService.analyze(validFile, "job description");

        assertThat(response.matchScore()).isEqualTo(80);
        assertThat(response.strengths()).containsExactly("Java");
        assertThat(response.resumeText()).isEqualTo("resume text");
    }

    @Test
    void analyze_nullFile_throwsIllegalArgument() {
        assertThatThrownBy(() -> analyzeService.analyze(null, "job description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No file provided");
    }

    @Test
    void analyze_emptyFile_throwsIllegalArgument() {
        when(validFile.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> analyzeService.analyze(validFile, "job description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No file provided");
    }

    @Test
    void analyze_wrongContentType_throwsIllegalArgument() {
        when(validFile.getContentType()).thenReturn("text/plain");
        assertThatThrownBy(() -> analyzeService.analyze(validFile, "job description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PDF");
    }

    @Test
    void analyze_fileTooLarge_throwsIllegalArgument() {
        when(validFile.getSize()).thenReturn(11 * 1024 * 1024L);
        assertThatThrownBy(() -> analyzeService.analyze(validFile, "job description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10MB");
    }

    @Test
    void analyze_blankJobDescription_throwsIllegalArgument() {
        assertThatThrownBy(() -> analyzeService.analyze(validFile, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job description");
    }
}
