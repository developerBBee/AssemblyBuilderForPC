package jp.developer.bbee.pcassem.presentation.data

class ReviewRequest {
    @JvmField var pcCase: ReviewItem? = null
    @JvmField var motherboard: ReviewItem? = null
    @JvmField var powerSupply: ReviewItem? = null
    @JvmField var cpu: ReviewItem? = null
    @JvmField var cpuCooler: ReviewItem? = null
    @JvmField var memories: List<ReviewItem>? = null
    @JvmField var storages: List<ReviewItem>? = null
    @JvmField var videoCards: List<ReviewItem>? = null

    fun buildPrompt(): String =
        "以下は自作PCパーツ構成です。\n" +
        "PC Case: $pcCase\n" +
        "Motherboard: $motherboard\n" +
        "Power Supply: $powerSupply\n" +
        "CPU: $cpu\n" +
        "CPU Cooler: $cpuCooler\n" +
        "Memory: $memories\n" +
        "Storage: $storages\n" +
        "Video Card: $videoCards\n" +
        "日本語で回答してください。" +
        "互換性に問題がないかチェックして問題があれば指摘、なければ互換性コメントなし。" +
        "構成でどのようなゲームが快適にプレイ可能か、300文字以内でレビューしてください。" +
        "レビュー以外のメッセージは不要です。"
}
