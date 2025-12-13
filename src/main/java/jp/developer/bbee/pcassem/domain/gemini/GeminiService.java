package jp.developer.bbee.pcassem.domain.gemini;

import jp.developer.bbee.pcassem.presentation.data.ReviewResponse;

public interface GeminiService {
    ReviewResponse getReview(String prompt);
}