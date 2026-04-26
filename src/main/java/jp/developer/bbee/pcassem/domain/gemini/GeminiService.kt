package jp.developer.bbee.pcassem.domain.gemini

import jp.developer.bbee.pcassem.presentation.data.ReviewResponse

interface GeminiService {
    fun getReview(prompt: String): ReviewResponse
}
