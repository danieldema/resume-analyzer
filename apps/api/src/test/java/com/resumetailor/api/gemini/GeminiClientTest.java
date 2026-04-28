package com.resumetailor.api.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GeminiClientTest {

    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() {
        geminiClient = spy(new GeminiClient(
                mock(WebClient.class), "test-key", "gemini-1.5-flash", new ObjectMapper()
        ));
    }

    @Test
    void analyzeSkillsGap_parsesValidJson() {
        String validJson = """
                {"matchScore":85,"strengths":["Java"],"weaknesses":["AWS"],"skillsGap":["Kubernetes"]}
                """;
        doReturn(validJson).when(geminiClient).callGemini(anyString());

        SkillsGapResult result = geminiClient.analyzeSkillsGap("resume text", "job description");

        assertThat(result.matchScore()).isEqualTo(85);
        assertThat(result.strengths()).containsExactly("Java");
        assertThat(result.weaknesses()).containsExactly("AWS");
        assertThat(result.skillsGap()).containsExactly("Kubernetes");
    }

    @Test
    void analyzeSkillsGap_badJson_throwsGeminiException() {
        doReturn("not valid json").when(geminiClient).callGemini(anyString());

        assertThatThrownBy(() -> geminiClient.analyzeSkillsGap("resume text", "job description"))
                .isInstanceOf(GeminiException.class);
    }
}
