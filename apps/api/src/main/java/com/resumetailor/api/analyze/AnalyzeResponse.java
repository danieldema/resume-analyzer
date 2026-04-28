package com.resumetailor.api.analyze;

import java.util.List;

public record AnalyzeResponse(
        int matchScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> skillsGap,
        String resumeText
) {}
