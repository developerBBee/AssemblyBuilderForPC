package jp.developer.bbee.pcassem.presentation.data

class ReviewResponse {
    var successData: ReviewSuccess? = null
    var failureData: ReviewFailure? = null

    constructor(data: ReviewSuccess) { successData = data }
    constructor(data: ReviewFailure) { failureData = data }
}
