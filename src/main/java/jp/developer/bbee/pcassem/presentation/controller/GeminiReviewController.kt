package jp.developer.bbee.pcassem.presentation.controller

import jp.developer.bbee.pcassem.domain.gemini.GeminiService
import jp.developer.bbee.pcassem.presentation.data.ReviewRequest
import jp.developer.bbee.pcassem.presentation.data.ReviewResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/gemini/review")
class GeminiReviewController(private val geminiService: GeminiService) {

    @PostMapping
    fun getGeminiReview(@RequestBody request: ReviewRequest): ResponseEntity<ReviewResponse> {
        val prompt = request.buildPrompt()
        println("[getGeminiReview] prompt=\n$prompt")
        return ResponseEntity.ok(geminiService.getReview(prompt))
    }
}
