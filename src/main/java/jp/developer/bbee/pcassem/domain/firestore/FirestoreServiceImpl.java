package jp.developer.bbee.pcassem.domain.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.model.SaveHead;
import jp.developer.bbee.pcassem.model.UserAssem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FirestoreServiceImpl implements FirestoreService {

    private static final int BATCH_LIMIT = 500;

    private final Firestore firestore;

    public FirestoreServiceImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void saveAssemblies(String firebaseUid, List<UserAssem> assemblies) throws Exception {
        if (assemblies.isEmpty()) return;

        for (int i = 0; i < assemblies.size(); i += BATCH_LIMIT) {
            List<UserAssem> chunk = assemblies.subList(i, Math.min(i + BATCH_LIMIT, assemblies.size()));
            WriteBatch batch = firestore.batch();
            for (UserAssem assem : chunk) {
                var ref = firestore.collection("users")
                        .document(firebaseUid)
                        .collection("assemblies")
                        .document(assem.deviceid());
                Map<String, Object> data = new HashMap<>();
                data.put("device", assem.device());
                data.put("createddate", Timestamp.of(java.sql.Timestamp.valueOf(assem.createddate())));
                data.put("lastupdate", Timestamp.of(java.sql.Timestamp.valueOf(assem.lastupdate())));
                batch.set(ref, data);
            }
            batch.commit().get();
        }
    }

    @Override
    public void saveSaves(String firebaseUid, String guestId,
                          List<SaveHead> saveHeads,
                          Map<String, List<SaveItem>> saveItemsMap) throws Exception {
        if (saveHeads.isEmpty()) return;

        for (int i = 0; i < saveHeads.size(); i += BATCH_LIMIT) {
            List<SaveHead> chunk = saveHeads.subList(i, Math.min(i + BATCH_LIMIT, saveHeads.size()));
            WriteBatch batch = firestore.batch();
            for (SaveHead head : chunk) {
                var ref = firestore.collection("saves").document(head.saveid());

                List<Map<String, Object>> items = new ArrayList<>();
                List<SaveItem> saveItems = saveItemsMap.getOrDefault(head.saveid(), List.of());
                for (SaveItem item : saveItems) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("deviceId", item.deviceId());
                    itemData.put("price", item.price());
                    itemData.put("createddate", Timestamp.of(java.sql.Timestamp.valueOf(item.createddate())));
                    itemData.put("lastupdate", Timestamp.of(java.sql.Timestamp.valueOf(item.lastupdate())));
                    items.add(itemData);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("firebaseUid", firebaseUid);
                data.put("guestId", guestId);
                data.put("saveName", head.savename());
                data.put("createddate", Timestamp.of(java.sql.Timestamp.valueOf(head.createddate())));
                data.put("lastupdate", Timestamp.of(java.sql.Timestamp.valueOf(head.lastupdate())));
                data.put("items", items);
                batch.set(ref, data);
            }
            batch.commit().get();
        }
    }

    @Override
    public List<UserAssem> getAssemblies(String firebaseUid) throws Exception {
        var docs = firestore.collection("users")
                .document(firebaseUid)
                .collection("assemblies")
                .get().get();
        List<UserAssem> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs.getDocuments()) {
            String deviceId = doc.getId();
            String device = doc.getString("device");
            Timestamp created = doc.getTimestamp("createddate");
            Timestamp updated = doc.getTimestamp("lastupdate");
            result.add(new UserAssem(
                    "",
                    deviceId,
                    device != null ? device : "",
                    "",
                    created != null ? created.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now(),
                    updated != null ? updated.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now()
            ));
        }
        return result;
    }

    @Override
    public List<SaveHead> getSaveHeadsRecent5(String firebaseUid) throws Exception {
        var docs = firestore.collection("saves")
                .whereEqualTo("firebaseUid", firebaseUid)
                .orderBy("lastupdate", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get().get();
        List<SaveHead> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs.getDocuments()) {
            String saveId = doc.getId();
            String guestId = doc.getString("guestId");
            String saveName = doc.getString("saveName");
            Timestamp created = doc.getTimestamp("createddate");
            Timestamp updated = doc.getTimestamp("lastupdate");
            result.add(new SaveHead(
                    saveId,
                    guestId != null ? guestId : "",
                    saveName != null ? saveName : "NONAME",
                    created != null ? created.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now(),
                    updated != null ? updated.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now()
            ));
        }
        return result;
    }
}
