package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.HomeController.DeviceInfo;
import jp.developer.bbee.pcassem.HomeController.RestoreDevice;
import jp.developer.bbee.pcassem.HomeController.SaveHead;
import jp.developer.bbee.pcassem.HomeController.UserAssem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeviceInfoDao {
    private final JdbcTemplate jdbcTemplate;
    record SqlDeviceInfo (String id, String device, String url, String name, String imgurl, String detail, Integer price, Integer rank, int flag1, int flag2,
                          String releasedate, Integer invisible, Timestamp createddate, Timestamp lastupdate) {
        static SqlDeviceInfo create(DeviceInfo di) {
            return new SqlDeviceInfo(
                    di.id(), di.device(), di.url(), di.name(), di.imgurl(), di.detail(), di.price(), di.rank(),
                    di.flag1(), di.flag2(), di.releasedate(), di.invisible(),
                    Timestamp.valueOf(di.createddate()), Timestamp.valueOf(di.lastupdate())
            );
        }
    }
    record SqlUserAssem (String id, String deviceid, String device, String guestid, Timestamp createddate, Timestamp lastupdate) {
        static SqlUserAssem create(UserAssem ua) {
            return new SqlUserAssem(
                    ua.id(), ua.deviceid(), ua.device(), ua.guestid(),
                    Timestamp.valueOf(ua.createddate()), Timestamp.valueOf(ua.lastupdate())
            );
        }
    }

    @Autowired // <- JdbcTemplate auto setting
    DeviceInfoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int setTime(LocalDateTime ldt) {
        return jdbcTemplate.update("UPDATE systemvals SET kakakuupdate = ?",
                Timestamp.valueOf(ldt));
    }

    public LocalDateTime getTime() {
        String query = "SELECT kakakuupdate FROM systemvals";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query).get(0);
            return ((Timestamp) result.get("kakakuupdate")).toLocalDateTime();
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            return LocalDateTime.of(2000,1,1,0,0,0);
        }
    }

    public void add(DeviceInfo deviceInfo) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(SqlDeviceInfo.create(deviceInfo));
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("devices");
        insert.execute(param);
    }

    public List<DeviceInfo> findAll(String device) {
        return findAll(device, 0);
    }

    public List<DeviceInfo> findAll(String device, int sortFlag) {
        String query = "SELECT * FROM devices WHERE device = ?";
        switch (sortFlag) {
            case 0 -> query += " ORDER BY rank";
            case 3 -> query += " ORDER BY releasedate DESC";
            case 1 -> query += " ORDER BY price";
            case 2 -> query += " ORDER BY price DESC";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, device);

        List<DeviceInfo> deviceInfoList = result.stream().map(
                (Map<String, Object> row) -> new DeviceInfo(
                        row.get("id") != null ? row.get("id").toString() : UUID.randomUUID().toString().replace("-", ""),
                        row.get("device") != null ? row.get("device").toString() : device,
                        row.get("url") != null ? row.get("url").toString() : "",
                        row.get("name") != null ? row.get("name").toString() : "",
                        row.get("imgurl") != null ? row.get("imgurl").toString() : "",
                        row.get("detail") != null ? row.get("detail").toString() : "",
                        row.get("price") != null ? (Integer) row.get("price") : 0,
                        row.get("rank") != null ? (Integer) row.get("rank") : 99,
                        row.get("flag1") != null ? (int) row.get("flag1") : 0,
                        row.get("flag2") != null ? (int) row.get("flag2") : 0,
                        row.get("releasedate") != null ? row.get("releasedate").toString() : "20000101",
                        row.get("invisible") != null ? (Integer) row.get("invisible") : 0,
                        row.get("createddate") != null ? ((Timestamp) row.get("createddate")).toLocalDateTime() : LocalDateTime.of(2000,1,1,0,0),
                        row.get("lastupdate") != null ? ((Timestamp) row.get("lastupdate")).toLocalDateTime() : LocalDateTime.of(2000,1,1,0,0)
                )).toList();

        return deviceInfoList;
    }

    public DeviceInfo findRecordByUrl(String url, String device) {
        String query = "SELECT * FROM devices WHERE device = ? AND url = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query, device, url).get(0);
            return new DeviceInfo(result.get("id").toString(), result.get("device").toString(), result.get("url").toString(),
                    result.get("name").toString(), result.get("imgurl").toString(), result.get("detail").toString(),
                    (Integer) result.get("price"), (Integer) result.get("rank"),
                    result.getOrDefault("flag1", 0) == null ? 0 : (int) result.getOrDefault("flag1", 0),
                    result.getOrDefault("flag2", 0) == null ? 0 : (int) result.getOrDefault("flag2", 0),
                    result.get("releasedate").toString(), (Integer) result.get("invisible"),
                    ((Timestamp) result.get("createddate")).toLocalDateTime(), ((Timestamp) result.get("lastupdate")).toLocalDateTime()
            );
        } catch (IndexOutOfBoundsException | ClassCastException e) {
            return null;
        }
    }

    public DeviceInfo findRecordById(String id) {
        String query = "SELECT * FROM devices WHERE id = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query, id).get(0);
            return new DeviceInfo(result.get("id").toString(), result.get("device").toString(), result.get("url").toString(),
                    result.get("name").toString(), result.get("imgurl").toString(), result.get("detail").toString(),
                    (Integer) result.get("price"), (Integer) result.get("rank"),
                    result.getOrDefault("flag1", 0) == null ? 0 : (int) result.getOrDefault("flag1", 0),
                    result.getOrDefault("flag2", 0) == null ? 0 : (int) result.getOrDefault("flag2", 0),
                    result.get("releasedate").toString(), (Integer) result.get("invisible"),
                    ((Timestamp) result.get("createddate")).toLocalDateTime(), ((Timestamp) result.get("lastupdate")).toLocalDateTime()
            );
        } catch (IndexOutOfBoundsException | ClassCastException e) {
            return null;
        }
    }

    public int delete(String id) {
        int number = jdbcTemplate.update("DELETE FROM devices WHERE id = ?", id);
        return number;
    }

    public int update(DeviceInfo deviceInfo) {
        int number = 0;
        try {
            number = jdbcTemplate.update(
                    "UPDATE devices SET device = ?, url = ?, name = ?, imgurl = ?, detail = ?, price = ?, rank = ?, flag1 = ?, flag2 = ?, releasedate = ?, invisible = ?, lastupdate = ? WHERE id = ?",
                    deviceInfo.device(), deviceInfo.url(), deviceInfo.name(), deviceInfo.imgurl(), deviceInfo.detail(),
                    deviceInfo.price(), deviceInfo.rank(), deviceInfo.flag1(), deviceInfo.flag2(), deviceInfo.releasedate(),
                    deviceInfo.invisible(), Timestamp.valueOf(deviceInfo.lastupdate()), deviceInfo.id());
            return number;
        } catch (DataIntegrityViolationException e) {
            System.out.println("name=" + deviceInfo.name() + " detail=" + deviceInfo.detail() +
                    " price=" + deviceInfo.price() + " rank=" + deviceInfo.rank());
        }
        return number;
    }

    public void rankReset(String device) {
        jdbcTemplate.update("UPDATE devices SET rank = 99 WHERE device = ?", device);
    }

    // UserAssem DAO
    public void addUserAssem(UserAssem assem) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(SqlUserAssem.create(assem));
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("assemblies");
        insert.execute(param);
    }

    public UserAssem findUserAssem(String deviceid, String guestid) {
        String query = "SELECT * FROM assemblies WHERE deviceid = ? AND guestid = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, deviceid, guestid);
        if (result.size() > 1) {
            System.out.println("duplicate record in UserAssem : deviceid=" + deviceid + " guestid=" +guestid);
        } else if (result.size() == 0){
            return null;
        }
        Map<String, Object> r = result.get(0);
        return new UserAssem(r.get("id").toString(), r.get("deviceid").toString(),
                r.get("device").toString(), r.get("guestid").toString(),
                ((Timestamp) r.get("createddate")).toLocalDateTime(),
                ((Timestamp) r.get("lastupdate")).toLocalDateTime());
    }

    public List<UserAssem> findAllUserAssemByGuestId(String guestid) {
        String query = "SELECT * FROM assemblies WHERE guestid = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, guestid);
        List<UserAssem> assembliesList = result.stream().map(
                (Map<String, Object> row) -> new UserAssem(
                        row.get("id").toString(),
                        row.get("deviceid").toString(),
                        row.get("device").toString(),
                        row.get("guestid").toString(),
                        ((Timestamp) row.get("createddate")).toLocalDateTime(),
                        ((Timestamp) row.get("lastupdate")).toLocalDateTime()
                )).toList();
        return assembliesList;
    }

    public int deleteUserAssem(String deviceid, String guestid) {
        String query = "SELECT * FROM assemblies WHERE deviceid = ? AND guestid = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, deviceid, guestid);
        if (result.size() > 1) {
            System.out.println("duplicate record in UserAssem : deviceid=" + deviceid + " guestid=" +guestid);
        } else if (result.size() == 0){
            System.out.println("no record in UserAssem : deviceid=" + deviceid + " guestid=" +guestid);
            return -1;
        }
        String id = result.get(0).get("id").toString();
        int number = jdbcTemplate.update("DELETE FROM assemblies WHERE id = ?", id);
        return number;
    }

    public Map<String, Integer> getAssemCountList(String guestid) {
        String query = "SELECT DEVICE, COUNT(*) AS COUNT FROM assemblies WHERE guestid = ? GROUP BY device";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, guestid);

        Map<String, Integer> countList = new HashMap<>();
        result.forEach((Map<String, Object> row) ->
                countList.put(row.get("DEVICE").toString(), Integer.parseInt(row.get("COUNT").toString())));

        return countList;
    }

    private static final String SAVE_QUERY = "INSERT INTO savelist VALUES(?, ?, ?, ?, ?)";
    record SqlSaveHead (String saveId, String guestId, String saveName, Timestamp createddate, Timestamp lastupdate) {}
    record SqlSaveInfo (String saveId, String deviceId, Integer price, Timestamp createddate, Timestamp lastupdate) {}

    @Transactional
    public void save(String saveId, String guestId, List<String> deviceIdList) {
        Timestamp tsNow = new Timestamp(System.currentTimeMillis());

        SqlSaveHead sqlSaveHead = new SqlSaveHead(saveId, guestId, "NONAME", tsNow, tsNow);
        SqlParameterSource param = new BeanPropertySqlParameterSource(sqlSaveHead);
        SimpleJdbcInsert insertHead = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("savehead");
        insertHead.execute(param);

        List<SqlSaveInfo> sqlSaveInfoList = deviceIdList.stream().map(
                di -> new SqlSaveInfo(saveId, di,
                        findRecordById(di).price(),
                        tsNow, tsNow)).toList();

        SqlParameterSource[] params =
                sqlSaveInfoList.stream().map(BeanPropertySqlParameterSource::new)
                        .toArray(BeanPropertySqlParameterSource[]::new);
                new BeanPropertySqlParameterSource(sqlSaveInfoList.get(0));
        SimpleJdbcInsert insertList = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("savelist");
        insertList.executeBatch(params);
    }

    private static final String RESTORE_QUERY =
            "SELECT savelist.saveid, savelist.deviceid, devices.device, devices.url" +
            ", devices.name, devices.imgurl, devices.detail" +
            ", savelist.price as oldprice, devices.price as newprice" +
            "  FROM savelist" +
            "  INNER JOIN devices ON savelist.deviceid = devices.id" +
            "  WHERE savelist.saveid = ?";
    public List<RestoreDevice> restore(String saveId) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(RESTORE_QUERY, saveId);
        return result.stream().map(
                (Map<String, Object> row) -> new RestoreDevice(
                        row.get("saveid").toString(),
                        row.get("deviceid").toString(),
                        row.get("device").toString(),
                        row.get("url").toString(),
                        row.get("name").toString(),
                        row.get("imgurl").toString(),
                        row.get("detail").toString(),
                        ((Integer) row.get("oldprice")),
                        ((Integer) row.get("newprice"))
                )).toList();
    }

    public List<SaveHead> getSaveHeadRecent5(String guestId) {
        String query = "SELECT * FROM savehead WHERE guestid = ? ORDER BY lastupdate desc";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, guestId);
        return result.stream().limit(5).map(
                (Map<String, Object> r) -> new SaveHead(
                        r.get("saveid").toString(),
                        r.get("guestid").toString(),
                        r.get("savename").toString(),
                        ((Timestamp) r.get("createddate")).toLocalDateTime(),
                        ((Timestamp) r.get("lastupdate")).toLocalDateTime()
                )).toList();
    }
}
