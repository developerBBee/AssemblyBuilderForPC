package jp.developer.bbee.pcassem.presentation.data

class ReviewResponse private constructor() {
    @JvmField var successData: ReviewSuccess? = null
    @JvmField var failureData: ReviewFailure? = null

    constructor(data: ReviewSuccess) : this() { successData = data }
    constructor(data: ReviewFailure) : this() { failureData = data }
}
