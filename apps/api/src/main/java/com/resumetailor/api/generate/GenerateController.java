package com.resumetailor.api.generate;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenerateController {

    private final GenerateService generateService;

    @PostMapping("/generate")
    @Operation(summary = "Generate resume suggestions or full PDF resume")
    public ResponseEntity<byte[]> generate(@RequestBody @Valid GenerateRequest req) {
        return generateService.generate(req);
    }
}
