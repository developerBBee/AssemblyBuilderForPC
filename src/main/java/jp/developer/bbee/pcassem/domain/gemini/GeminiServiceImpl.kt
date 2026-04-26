package jp.developer.bbee.pcassem.domain.gemini

import com.google.genai.Client
import jp.developer.bbee.pcassem.presentation.data.ReviewFailure
import jp.developer.bbee.pcassem.presentation.data.ReviewResponse
import jp.developer.bbee.pcassem.presentation.data.ReviewSuccess
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private const val MODEL_NAME = "gemini-2.5-flash"

@Service
class GeminiServiceImpl(@Value("\${gemini.api.key}") key: String) : GeminiService {

    private val logger = LoggerFactory.getLogger(GeminiServiceImpl::class.java)
    private val geminiClient = Client.builder().apiKey(key).build()

    override fun getReview(prompt: String): ReviewResponse {
        return try {
            val response = geminiClient.models.generateContent(MODEL_NAME, prompt, null)
            val text = response.text()
            if (text != null) ReviewResponse(ReviewSuccess(text))
            else ReviewResponse(ReviewFailure("レビューの取得に失敗しました（空レスポンス）"))
        } catch (e: Exception) {
            logger.error("Error during Gemini API call", e)
            ReviewResponse(ReviewFailure(e.message ?: "レビューの取得に失敗しました"))
        }
    }
}
