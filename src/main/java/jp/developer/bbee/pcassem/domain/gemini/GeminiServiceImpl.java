package jp.developer.bbee.pcassem.domain.gemini;

import com.google.genai.Client;
import jp.developer.bbee.pcassem.presentation.data.ReviewFailure;
import jp.developer.bbee.pcassem.presentation.data.ReviewResponse;
import jp.developer.bbee.pcassem.presentation.data.ReviewSuccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiServiceImpl implements GeminiService {
    private static final String MODEL_NAME = "gemini-2.5-flash";

    private final Client geminiClient;

    public GeminiServiceImpl(@Value("${gemini.api.key}") String key) {
        this.geminiClient = Client.builder().apiKey(key).build();
    }

    @Override
    public ReviewResponse getReview(String prompt) {
        try {
            var response = geminiClient.models.generateContent(MODEL_NAME, prompt, null);
            var successText = response.text();
            var successData = new ReviewSuccess(successText);
            return new ReviewResponse(successData);
        } catch (Exception e) {
            System.out.println("Error during Gemini API call: " + e.getMessage());
            var failureData = new ReviewFailure(e.getMessage());
            return new ReviewResponse(failureData);
        }
    }
}
