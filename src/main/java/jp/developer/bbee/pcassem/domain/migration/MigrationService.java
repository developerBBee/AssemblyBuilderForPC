package jp.developer.bbee.pcassem.domain.migration;

public interface MigrationService {
    /**
     * Firebase IDトークンで認証し、guestIdに紐づくH2データをFirestoreに移行する。
     * 移行済みの場合は何もしない（べき等）。
     */
    void migrate(String idToken, String guestId) throws Exception;
}
