package com.resumetailor.api.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumetailor.api.docx.DocxGenerator;
import com.resumetailor.api.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final DocxGenerator docxGenerator;

    public ResponseEntity<byte[]> generate(GenerateRequest req) {
        return switch (req.outputMode()) {
            case "SUGGESTIONS" -> {
                String suggestions = geminiClient.generateSuggestions(
                        req.resumeText(), req.jobDescription(), req.confirmedSkills());
                try {
                    byte[] body = objectMapper.writeValueAsBytes(new GenerateResponse(suggestions));
                    yield ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize response", e);
                }
            }
            case "FULL_RESUME" -> {
                String resumeContent = geminiClient.generateFullResume(
                        req.resumeText(), req.jobDescription(), req.confirmedSkills());
                try {
                    byte[] pdf = docxGenerator.generatePdf(resumeContent);
                    yield ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"tailored-resume.pdf\"")
                            .body(pdf);
                } catch (Exception e) {
                    throw new RuntimeException("PDF generation failed", e);
                }
            }
            default -> throw new IllegalArgumentException(
                    "Invalid outputMode: " + req.outputMode());
        };
    }
}
