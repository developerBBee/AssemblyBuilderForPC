package jp.developer.bbee.pcassem.domain.gemini

import com.google.genai.Client
import jp.developer.bbee.pcassem.presentation.data.ReviewFailure
import jp.developer.bbee.pcassem.presentation.data.ReviewResponse
import jp.developer.bbee.pcassem.presentation.data.ReviewSuccess
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private const val MODEL_NAME = "gemini-2.5-flash"

@Service
class GeminiServiceImpl(@Value("\${gemini.api.key}") key: String) : GeminiService {

    private val geminiClient = Client.builder().apiKey(key).build()

    override fun getReview(prompt: String): ReviewResponse {
        return try {
            val response = geminiClient.models.generateContent(MODEL_NAME, prompt, null)
            ReviewResponse(ReviewSuccess(response.text() ?: ""))
        } catch (e: Exception) {
            println("Error during Gemini API call: $e")
            ReviewResponse(ReviewFailure(e.message ?: "レビューの取得に失敗しました"))
        }
    }
}
