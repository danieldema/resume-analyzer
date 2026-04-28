package com.resumetailor.api.generate;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GenerateRequest(
        @NotBlank String resumeText,
        @NotBlank String jobDescription,
        List<String> confirmedSkills,
        @NotBlank String outputMode
) {}
