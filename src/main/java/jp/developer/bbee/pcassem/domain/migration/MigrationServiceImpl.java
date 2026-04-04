package jp.developer.bbee.pcassem.domain.migration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jp.developer.bbee.pcassem.DeviceInfoDao;
import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.HomeController.SaveHead;
import jp.developer.bbee.pcassem.HomeController.UserAssem;
import jp.developer.bbee.pcassem.UidMappingDao;
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    public void migrate(String idToken, String guestId) throws Exception {
        // 1. Firebase IDトークンを検証してUIDを取得
        FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String firebaseUid = token.getUid();

        // 2. 移行済みチェック（べき等）
        if (uidMappingDao.findByFirebaseUid(firebaseUid) != null) {
            System.out.println("[Migration] Already migrated. uid=" + firebaseUid);
            return;
        }

        // 3. H2からユーザーデータ取得
        List<UserAssem> assemblies = dao.findAllUserAssemByGuestId(guestId);
        List<SaveHead> saveHeads = dao.getSaveHeadAll(guestId);

        Map<String, List<SaveItem>> saveItemsMap = new HashMap<>();
        for (SaveHead head : saveHeads) {
            saveItemsMap.put(head.saveid(), dao.getSaveItemsBySaveId(head.saveid()));
        }

        // 4. Firestoreへ書き込み
        firestoreService.saveAssemblies(firebaseUid, assemblies);
        firestoreService.saveSaves(firebaseUid, guestId, saveHeads, saveItemsMap);

        // 5. 移行完了を記録
        uidMappingDao.insert(firebaseUid, guestId);

        // 6. H2から削除
        dao.deleteAllUserAssemByGuestId(guestId);
        dao.deleteAllSavesByGuestId(guestId);

        System.out.println("[Migration] Completed. uid=" + firebaseUid
                + " assemblies=" + assemblies.size() + " saves=" + saveHeads.size());
    }
}
