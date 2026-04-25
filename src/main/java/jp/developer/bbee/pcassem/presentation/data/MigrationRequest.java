package jp.developer.bbee.pcassem.presentation.data;

public class MigrationRequest {
    public String idToken;   // Firebase IDトークン（クライアントから送信）
    public String guestId;   // 移行元の guestId（localStorage から送信）
}
