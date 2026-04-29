package com.resumetailor.api.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatRequest(
        @NotBlank String resumeText,
        @NotBlank String jobDescription,
        @NotEmpty List<ChatMessage> messages
) {}
