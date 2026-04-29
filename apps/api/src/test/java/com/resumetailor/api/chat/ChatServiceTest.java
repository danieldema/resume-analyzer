package com.resumetailor.api.chat;

import com.resumetailor.api.gemini.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private GeminiClient geminiClient;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        geminiClient = mock(GeminiClient.class);
        chatService = new ChatService(geminiClient);
    }

    @Test
    void chat_wrapsTokensAsSseEventsWithDoneAtEnd() {
        when(geminiClient.streamChat(anyString(), anyString(), any()))
                .thenReturn(Flux.just("Hello", " world"));

        ChatRequest req = new ChatRequest(
                "resume text", "job description",
                List.of(new ChatMessage("user", "How can I improve my skills section?")));

        List<ServerSentEvent<String>> events = chatService.chat(req).collectList().block();

        assertThat(events).hasSize(3);
        assertThat(events.get(0).data()).isEqualTo("Hello");
        assertThat(events.get(1).data()).isEqualTo(" world");
        assertThat(events.get(2).event()).isEqualTo("done");
        assertThat(events.get(2).data()).isEmpty();
    }

    @Test
    void chat_emptyTokenStream_emitsOnlyDoneEvent() {
        when(geminiClient.streamChat(anyString(), anyString(), any()))
                .thenReturn(Flux.empty());

        ChatRequest req = new ChatRequest(
                "resume text", "job description",
                List.of(new ChatMessage("user", "Hello")));

        List<ServerSentEvent<String>> events = chatService.chat(req).collectList().block();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).event()).isEqualTo("done");
    }
}
