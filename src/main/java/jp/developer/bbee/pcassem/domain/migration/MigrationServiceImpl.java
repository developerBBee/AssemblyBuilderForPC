package jp.developer.bbee.pcassem.domain.migration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jp.developer.bbee.pcassem.DeviceInfoDao;
import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.model.SaveHead;
import jp.developer.bbee.pcassem.model.UserAssem;
import jp.developer.bbee.pcassem.UidMappingDao;
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MigrationServiceImpl implements MigrationService {

    private final DeviceInfoDao dao;
    private final UidMappingDao uidMappingDao;
    private final FirestoreService firestoreService;

    public MigrationServiceImpl(DeviceInfoDao dao,
                                 UidMappingDao uidMappingDao,
                                 FirestoreService firestoreService) {
        this.dao = dao;
        this.uidMappingDao = uidMappingDao;
        this.firestoreService = firestoreService;
    }

    @Override
    public boolean migrate(String idToken, String guestId) throws Exception {
        // 1. Firebase IDトークンを検証してUIDを取得
        FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String firebaseUid = token.getUid();

        // 2. 移行済みチェック（べき等）— UID・guestId 両方で確認
        if (uidMappingDao.findByFirebaseUid(firebaseUid) != null) {
            System.out.println("[Migration] Already migrated. uid=" + firebaseUid);
            return false;
        }
        if (uidMappingDao.findByGuestId(guestId) != null) {
            System.out.println("[Migration] guestId already migrated under a different uid. guestId=" + guestId);
            return false;
        }

        // 3. H2からユーザーデータ取得（N+1回避のためsaveItemsを一括取得）
        List<UserAssem> assemblies = dao.findAllUserAssemByGuestId(guestId);
        List<SaveHead> saveHeads = dao.getSaveHeadAll(guestId);
        Map<String, List<SaveItem>> saveItemsMap = dao.getSaveItemsByGuestId(guestId);

        // 4. Firestoreへ書き込み
        firestoreService.saveAssemblies(firebaseUid, assemblies);
        firestoreService.saveSaves(firebaseUid, guestId, saveHeads, saveItemsMap);

        // 5. H2から削除（先に行うことでFirestore書き込み済みの場合のリトライを安全にする）
        dao.deleteAllUserAssemByGuestId(guestId);
        dao.deleteAllSavesByGuestId(guestId);

        // 6. 移行完了を記録（H2削除成功後）
        uidMappingDao.insert(firebaseUid, guestId);

        System.out.println("[Migration] Completed. uid=" + firebaseUid
                + " assemblies=" + assemblies.size() + " saves=" + saveHeads.size());
        return true;
    }
}
