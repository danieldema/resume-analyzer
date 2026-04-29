package com.resumetailor.api.chat;

import com.resumetailor.api.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiClient geminiClient;

    public Flux<ServerSentEvent<String>> chat(ChatRequest req) {
        return geminiClient.streamChat(req.resumeText(), req.jobDescription(), req.messages())
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }
}
