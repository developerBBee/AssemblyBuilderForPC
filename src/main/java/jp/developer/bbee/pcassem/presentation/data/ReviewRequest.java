package jp.developer.bbee.pcassem.presentation.data;

/**
 * 自作PCパーツ構成のレビューリクエストデータクラス
 */
public class ReviewRequest {
    public ReviewItem pcCase;
    public ReviewItem motherboard;
    public ReviewItem cpu;
    public ReviewItem graphicsCard;
    public ReviewItem memory;
    public ReviewItem storage;
    public ReviewItem powerSupply;

    public String buildPrompt() {
        return "以下は自作PCパーツ構成です。\n" +
                "PC Case: " + pcCase + "\n" +
                "Motherboard: " + motherboard + "\n" +
                "CPU: " + cpu + "\n" +
                "Graphics Card: " + graphicsCard + "\n" +
                "Memory: " + memory + "\n" +
                "Storage: " + storage + "\n" +
                "Power Supply: " + powerSupply + "\n" +
                "互換性に問題がないかチェックして問題があれば指摘、なければ互換性コメントなし。" +
                "構成でどのようなゲームが快適にプレイ可能か、300文字以内でレビューしてください。" +
                "レビュー以外のメッセージは不要です。";
    }
}
