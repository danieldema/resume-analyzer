package com.resumetailor.api.exception;

import com.resumetailor.api.gemini.GeminiException;
import com.resumetailor.api.pdf.PdfExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PdfExtractionException.class)
    public ResponseEntity<Map<String, String>> handlePdfError(PdfExtractionException e) {
        return ResponseEntity.unprocessableEntity()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(GeminiException.class)
    public ResponseEntity<Map<String, String>> handleGeminiError(GeminiException e) {
        log.error("Gemini error", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Analysis failed. Please try again."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Something went wrong. Please try again."));
    }
}
