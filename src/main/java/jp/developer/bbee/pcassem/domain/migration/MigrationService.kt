package jp.developer.bbee.pcassem.domain.migration

interface MigrationService {
    /**
     * Firebase IDトークンで認証し、guestIdに紐づくH2データをFirestoreに移行する。
     * @return true=移行実施, false=移行済みによりスキップ
     */
    @Throws(Exception::class)
    fun migrate(idToken: String, guestId: String): Boolean
}
