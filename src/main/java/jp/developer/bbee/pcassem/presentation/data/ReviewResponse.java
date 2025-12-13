package jp.developer.bbee.pcassem.presentation.data;

public class ReviewResponse {
    public ReviewSuccess successData = null;
    public ReviewFailure failureData = null;

    public ReviewResponse(ReviewSuccess data) {
        this.successData = data;
    }

    public ReviewResponse(ReviewFailure data) {
        this.failureData = data;
    }
}
