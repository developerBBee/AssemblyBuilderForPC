package jp.developer.bbee.pcassem.domain.firestore;

import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.HomeController.SaveHead;
import jp.developer.bbee.pcassem.HomeController.UserAssem;

import java.util.List;
import java.util.Map;

public interface FirestoreService {
    void saveAssemblies(String firebaseUid, List<UserAssem> assemblies) throws Exception;
    void saveSaves(String firebaseUid, String guestId, List<SaveHead> saveHeads,
                   Map<String, List<SaveItem>> saveItemsMap) throws Exception;
}
