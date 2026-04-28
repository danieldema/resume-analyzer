package com.resumetailor.api.gemini;

import java.util.List;

public record SkillsGapResult(
        int matchScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> skillsGap
) {}
