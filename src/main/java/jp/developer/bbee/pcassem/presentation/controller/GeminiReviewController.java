package jp.developer.bbee.pcassem.presentation.controller;

import jp.developer.bbee.pcassem.domain.gemini.GeminiService;
import jp.developer.bbee.pcassem.presentation.data.ReviewRequest;
import jp.developer.bbee.pcassem.presentation.data.ReviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/gemini/review")
public class GeminiReviewController {

    private final GeminiService geminiService;
    public GeminiReviewController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> getGeminiReview(@RequestBody ReviewRequest request) {
        String prompt = request.buildPrompt();
        System.out.println("[getGeminiReview] prompt=\n" + prompt);
        return ResponseEntity.ok(geminiService.getReview(prompt));
    }
}
