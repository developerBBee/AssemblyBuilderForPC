package jp.developer.bbee.pcassem.domain.auth;

public interface IdTokenVerifier {
    /**
     * Firebase ID トークンを検証し、UID を返す。
     * 検証失敗時は例外をスローする。
     */
    String verifyAndGetUid(String idToken) throws Exception;
}
