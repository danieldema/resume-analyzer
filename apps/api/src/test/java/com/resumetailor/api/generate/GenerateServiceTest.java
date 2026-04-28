package com.resumetailor.api.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumetailor.api.gemini.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GenerateServiceTest {

    private GeminiClient geminiClient;
    private GenerateService generateService;

    @BeforeEach
    void setUp() {
        geminiClient = mock(GeminiClient.class);
        generateService = new GenerateService(geminiClient, new ObjectMapper());
    }

    @Test
    void generate_suggestions_returnsJsonResponse() throws Exception {
        when(geminiClient.generateSuggestions(anyString(), anyString(), any()))
                .thenReturn("Add Docker to your skills section.");

        GenerateRequest req = new GenerateRequest(
                "resume text", "job description", List.of("Docker"), "SUGGESTIONS");

        ResponseEntity<byte[]> response = generateService.generate(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = new String(response.getBody());
        assertThat(body).contains("Add Docker to your skills section.");
    }

    @Test
    void generate_fullResume_throwsUnsupportedOperation() {
        GenerateRequest req = new GenerateRequest(
                "resume text", "job description", List.of(), "FULL_RESUME");

        assertThatThrownBy(() -> generateService.generate(req))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void generate_invalidOutputMode_throwsIllegalArgument() {
        GenerateRequest req = new GenerateRequest(
                "resume text", "job description", List.of(), "INVALID");

        assertThatThrownBy(() -> generateService.generate(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid outputMode");
    }
}
