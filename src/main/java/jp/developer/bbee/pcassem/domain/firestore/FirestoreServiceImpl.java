package jp.developer.bbee.pcassem.domain.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.HomeController.SaveHead;
import jp.developer.bbee.pcassem.HomeController.UserAssem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FirestoreServiceImpl implements FirestoreService {

    private final Firestore firestore;

    public FirestoreServiceImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void saveAssemblies(String firebaseUid, List<UserAssem> assemblies) throws Exception {
        if (assemblies.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (UserAssem assem : assemblies) {
            var ref = firestore.collection("users")
                    .document(firebaseUid)
                    .collection("assemblies")
                    .document(assem.deviceid());
            Map<String, Object> data = new HashMap<>();
            data.put("device", assem.device());
            data.put("createddate", Timestamp.of(
                    java.sql.Timestamp.valueOf(assem.createddate())));
            batch.set(ref, data);
        }
        batch.commit().get();
    }

    @Override
    public void saveSaves(String firebaseUid, String guestId,
                          List<SaveHead> saveHeads,
                          Map<String, List<SaveItem>> saveItemsMap) throws Exception {
        if (saveHeads.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (SaveHead head : saveHeads) {
            var ref = firestore.collection("saves").document(head.saveid());

            List<Map<String, Object>> items = new ArrayList<>();
            List<SaveItem> saveItems = saveItemsMap.getOrDefault(head.saveid(), List.of());
            for (SaveItem item : saveItems) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("deviceId", item.deviceId());
                itemData.put("price", item.price());
                items.add(itemData);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("firebaseUid", firebaseUid);
            data.put("guestId", guestId);
            data.put("saveName", head.savename());
            data.put("createddate", Timestamp.of(
                    java.sql.Timestamp.valueOf(head.createddate())));
            data.put("items", items);
            batch.set(ref, data);
        }
        batch.commit().get();
    }
}
