package jp.developer.bbee.pcassem.domain.migration

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao
import jp.developer.bbee.pcassem.data.dao.UidMappingDao
import jp.developer.bbee.pcassem.domain.auth.IdTokenVerifier
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MigrationServiceImpl(
    private val dao: DeviceInfoDao,
    private val uidMappingDao: UidMappingDao,
    private val firestoreService: FirestoreService,
    private val idTokenVerifier: IdTokenVerifier,
) : MigrationService {

    private val logger = LoggerFactory.getLogger(MigrationServiceImpl::class.java)

    override fun migrate(idToken: String, guestId: String): Boolean {
        val firebaseUid = idTokenVerifier.verifyAndGetUid(idToken)

        if (uidMappingDao.findByFirebaseUid(firebaseUid) != null) {
            logger.info("[Migration] Already migrated. uid={}", firebaseUid)
            return false
        }
        if (uidMappingDao.findByGuestId(guestId) != null) {
            logger.info("[Migration] guestId already migrated under a different uid. guestId={}", guestId)
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

        logger.info("[Migration] Completed. uid={} assemblies={} saves={}", firebaseUid, assemblies.size, saveHeads.size)
        return true
    }
}
