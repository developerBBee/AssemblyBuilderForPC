package jp.developer.bbee.pcassem.domain.migration

import com.google.firebase.auth.FirebaseAuth
import jp.developer.bbee.pcassem.DeviceInfoDao
import jp.developer.bbee.pcassem.UidMappingDao
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService
import org.springframework.stereotype.Service

@Service
class MigrationServiceImpl(
    private val dao: DeviceInfoDao,
    private val uidMappingDao: UidMappingDao,
    private val firestoreService: FirestoreService,
) : MigrationService {

    override fun migrate(idToken: String, guestId: String): Boolean {
        val firebaseUid = FirebaseAuth.getInstance().verifyIdToken(idToken).uid

        if (uidMappingDao.findByFirebaseUid(firebaseUid) != null) {
            println("[Migration] Already migrated. uid=$firebaseUid")
            return false
        }
        if (uidMappingDao.findByGuestId(guestId) != null) {
            println("[Migration] guestId already migrated under a different uid. guestId=$guestId")
            return false
        }

        val assemblies = dao.findAllUserAssemByGuestId(guestId)
        val saveHeads = dao.getSaveHeadAll(guestId)
        val saveItemsMap = dao.getSaveItemsByGuestId(guestId)

        firestoreService.saveAssemblies(firebaseUid, assemblies)
        firestoreService.saveSaves(firebaseUid, guestId, saveHeads, saveItemsMap)

        dao.deleteAllUserAssemByGuestId(guestId)
        dao.deleteAllSavesByGuestId(guestId)

        uidMappingDao.insert(firebaseUid, guestId)

        println("[Migration] Completed. uid=$firebaseUid assemblies=${assemblies.size} saves=${saveHeads.size}")
        return true
    }
}
