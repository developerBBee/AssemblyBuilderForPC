package jp.developer.bbee.pcassem.domain.firestore

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao.SaveItem
import jp.developer.bbee.pcassem.domain.model.SaveHead
import jp.developer.bbee.pcassem.domain.model.UserAssem
import java.util.Optional

interface FirestoreService {
    @Throws(Exception::class)
    fun saveAssemblies(firebaseUid: String, assemblies: List<UserAssem>)

    @Throws(Exception::class)
    fun saveSaves(
        firebaseUid: String,
        guestId: String?,
        saveHeads: List<SaveHead>,
        saveItemsMap: Map<String, @JvmSuppressWildcards List<SaveItem>>,
    )

    @Throws(Exception::class)
    fun getAssemblies(firebaseUid: String): List<UserAssem>

    @Throws(Exception::class)
    fun getSaveHeadsRecent5(firebaseUid: String): List<SaveHead>

    @Throws(Exception::class)
    fun addAssembly(firebaseUid: String, assem: UserAssem)

    @Throws(Exception::class)
    fun deleteAssembly(firebaseUid: String, deviceId: String)

    /**
     * saves/{saveId} の items を返す。
     * ドキュメントが存在しない場合のみ Optional.empty() を返す（H2 フォールバック用）。
     * ドキュメントが存在する場合は items の有無にかかわらず Optional.of(items) を返す（空リストを含む）。
     */
    @Throws(Exception::class)
    fun getSaveItems(saveId: String): Optional<List<SaveItem>>
}
