package com.resumetailor.api.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public GeminiClient(WebClient geminiWebClient,
                        @Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String model,
                        ObjectMapper objectMapper) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
    }

    public SkillsGapResult analyzeSkillsGap(String resumeText, String jobDescription) {
        String prompt = """
                You are a sharp resume coach specializing in tech resumes. Compare this
                resume against the job description.
                Return a JSON object with exactly these fields:
                - matchScore: integer 0-100
                - strengths: array of strings (max 5)
                - weaknesses: array of strings (max 5)
                - skillsGap: array of strings — specific skills, tools, or qualifications
                  mentioned in the job description that are not clearly evidenced in the
                  resume (max 8, prioritised by relevance)

                Resume text:
                %s

                Job description:
                %s

                Return only valid JSON, no markdown fences.
                """.formatted(resumeText, jobDescription);

        String raw = callGemini(prompt);
        try {
            return objectMapper.readValue(raw, SkillsGapResult.class);
        } catch (JsonProcessingException e) {
            throw new GeminiException("Failed to parse Gemini response: " + raw);
        }
    }

    String callGemini(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );
        String response = webClient.post()
                .uri("/{model}:generateContent?key={key}", model, apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return extractText(response);
    }

    private String extractText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.at("/candidates/0/content/parts/0/text").asText();
        } catch (Exception e) {
            throw new GeminiException("Unexpected Gemini response structure");
        }
    }
}
