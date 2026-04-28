package com.resumetailor.api.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumetailor.api.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

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
            case "FULL_RESUME" -> throw new UnsupportedOperationException(
                    "PDF generation not yet implemented");
            default -> throw new IllegalArgumentException(
                    "Invalid outputMode: " + req.outputMode());
        };
    }
}
