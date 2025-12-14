package jp.developer.bbee.pcassem.presentation.data;

import java.util.List;

/**
 * 自作PCパーツ構成のレビューリクエストデータクラス
 */
public class ReviewRequest {
    public ReviewItem pcCase;
    public ReviewItem motherboard;
    public ReviewItem powerSupply;
    public ReviewItem cpu;
    public ReviewItem cpuCooler;
    public List<ReviewItem> memories;
    public List<ReviewItem> storages;
    public List<ReviewItem> videoCards;

    public String buildPrompt() {
        return "以下は自作PCパーツ構成です。\n" +
                "PC Case: " + pcCase + "\n" +
                "Motherboard: " + motherboard + "\n" +
                "Power Supply: " + powerSupply + "\n" +
                "CPU: " + cpu + "\n" +
                "CPU Cooler: " + cpuCooler + "\n" +
                "Memory: " + memories + "\n" +
                "Storage: " + storages + "\n" +
                "Video Card: " + videoCards + "\n" +
                "日本語で回答してください。" +
                "互換性に問題がないかチェックして問題があれば指摘、なければ互換性コメントなし。" +
                "構成でどのようなゲームが快適にプレイ可能か、300文字以内でレビューしてください。" +
                "レビュー以外のメッセージは不要です。";
    }
}
