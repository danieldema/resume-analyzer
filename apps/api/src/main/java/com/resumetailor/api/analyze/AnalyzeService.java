package com.resumetailor.api.analyze;

import com.resumetailor.api.gemini.GeminiClient;
import com.resumetailor.api.gemini.SkillsGapResult;
import com.resumetailor.api.pdf.PdfExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnalyzeService {

    private final PdfExtractor pdfExtractor;
    private final GeminiClient geminiClient;

    public AnalyzeResponse analyze(MultipartFile file, String jobDescription) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("No file provided.");
        if (!"application/pdf".equals(file.getContentType()))
            throw new IllegalArgumentException("File must be a PDF.");
        if (file.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("File must be under 10MB.");
        if (jobDescription == null || jobDescription.isBlank())
            throw new IllegalArgumentException("Job description is required.");

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read uploaded file.");
        }

        String resumeText = pdfExtractor.extract(bytes);
        SkillsGapResult result = geminiClient.analyzeSkillsGap(resumeText, jobDescription);

        return new AnalyzeResponse(
                result.matchScore(),
                result.strengths(),
                result.weaknesses(),
                result.skillsGap(),
                resumeText
        );
    }
}
