package jp.developer.bbee.pcassem.domain.firestore;

import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import jp.developer.bbee.pcassem.model.SaveHead;
import jp.developer.bbee.pcassem.model.UserAssem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FirestoreService {
    void saveAssemblies(String firebaseUid, List<UserAssem> assemblies) throws Exception;
    void saveSaves(String firebaseUid, String guestId, List<SaveHead> saveHeads,
                   Map<String, List<SaveItem>> saveItemsMap) throws Exception;
    List<UserAssem> getAssemblies(String firebaseUid) throws Exception;
    List<SaveHead> getSaveHeadsRecent5(String firebaseUid) throws Exception;
    void addAssembly(String firebaseUid, UserAssem assem) throws Exception;
    void deleteAssembly(String firebaseUid, String deviceId) throws Exception;
    /** saves/{saveId} の items を返す。ドキュメントが存在しない場合は Optional.empty()、存在する場合は Optional.of(items) を返す。 */
    Optional<List<SaveItem>> getSaveItems(String saveId) throws Exception;
}
