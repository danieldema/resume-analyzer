package com.resumetailor.api.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumetailor.api.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
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

        String raw = stripMarkdownFences(callGemini(prompt));
        try {
            return objectMapper.readValue(raw, SkillsGapResult.class);
        } catch (JsonProcessingException e) {
            throw new GeminiException("Failed to parse Gemini response: " + raw);
        }
    }

    public String generateSuggestions(String resumeText, String jobDescription,
                                      List<String> confirmedSkills) {
        String skills = confirmedSkills == null || confirmedSkills.isEmpty()
                ? "none specified"
                : String.join(", ", confirmedSkills);
        String prompt = """
                You are a resume coach specializing in tech resumes. Based on the resume,
                job description, and the candidate's confirmed skills below, provide specific,
                actionable suggestions for tailoring this resume.
                Focus on: what to reword, what to add, what to cut, and how to frame confirmed skills.
                Confirmed skills: %s

                Resume text:
                %s

                Job description:
                %s
                """.formatted(skills, resumeText, jobDescription);
        return callGemini(prompt);
    }

    public String generateFullResume(String resumeText, String jobDescription,
                                     List<String> confirmedSkills) {
        String skills = confirmedSkills == null || confirmedSkills.isEmpty()
                ? "none to add"
                : String.join(", ", confirmedSkills);
        String prompt = """
                You are a resume coach specializing in tech resumes. Rewrite the following
                resume to be tailored for the job description below.
                The candidate has confirmed they possess these additional skills: %s.
                Incorporate them naturally. Do not fabricate experience.
                Keep the same basic structure. Return the full rewritten resume as plain
                text, ready to be formatted into a document.

                Original resume:
                %s

                Job description:
                %s
                """.formatted(skills, resumeText, jobDescription);
        return callGemini(prompt);
    }

    public Flux<String> streamChat(String resumeText, String jobDescription,
                                   List<ChatMessage> messages) {
        List<Map<String, Object>> contents = buildChatContents(resumeText, jobDescription, messages);

        return webClient.post()
                .uri("/{model}:streamGenerateContent?alt=sse&key={key}", model, apiKey)
                .bodyValue(Map.of("contents", contents))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6))
                .filter(json -> !json.equals("[DONE]"))
                .mapNotNull(this::extractToken);
    }

    private String extractToken(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String text = node.at("/candidates/0/content/parts/0/text").asText(null);
            return (text != null && !text.isEmpty()) ? text : null;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> buildChatContents(String resumeText, String jobDescription,
                                                         List<ChatMessage> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text",
                        "You are a resume coach helping a candidate tailor their resume. " +
                        "Here is their resume:\n\n" + resumeText +
                        "\n\nJob description they're targeting:\n\n" + jobDescription +
                        "\n\nHelp them improve their resume for this role."))));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text",
                        "Understood. I've reviewed the resume and job description. " +
                        "I'm ready to help tailor the resume for this role."))));

        for (ChatMessage msg : messages) {
            String geminiRole = "assistant".equals(msg.role()) ? "model" : "user";
            contents.add(Map.of(
                    "role", geminiRole,
                    "parts", List.of(Map.of("text", msg.content()))));
        }

        return contents;
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
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(errorBody -> {
                                    log.error("Gemini error {}: {}", clientResponse.statusCode(), errorBody);
                                    return new GeminiException("Gemini returned " + clientResponse.statusCode() + ": " + errorBody);
                                }))
                .bodyToMono(String.class)
                .block();
        return extractText(response);
    }

    private String stripMarkdownFences(String text) {
        String stripped = text.strip();
        if (stripped.startsWith("```")) {
            stripped = stripped.replaceFirst("```(?:json)?\\s*", "");
            stripped = stripped.replaceFirst("\\s*```$", "");
        }
        return stripped.strip();
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
