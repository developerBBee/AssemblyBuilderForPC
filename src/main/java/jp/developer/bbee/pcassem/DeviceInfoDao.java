package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.HomeController.DeviceInfo;
import jp.developer.bbee.pcassem.HomeController.UserAssem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DeviceInfoDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired // <- JdbcTemplate auto setting
    DeviceInfoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(DeviceInfo deviceInfo) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(deviceInfo);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("devices");
        insert.execute(param);
    }

    public List<DeviceInfo> findAll(String device) {
        String query = "SELECT * FROM devices WHERE device = ?";
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
                        row.get("rank") != null ? (Integer) row.get("rank") : 99
                )).toList();

        return deviceInfoList;
    }

    public DeviceInfo findRecordByUrl(String url, String device) {
        String query = "SELECT * FROM devices WHERE device = ? AND url = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query, device, url).get(0);
            return new DeviceInfo(result.get("id").toString(), result.get("device").toString(), result.get("url").toString(),
                    result.get("name").toString(), result.get("imgurl").toString(), result.get("detail").toString(),
                    (Integer) result.get("price"), (Integer) result.get("rank"));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public DeviceInfo findRecordById(String id) {
        String query = "SELECT * FROM devices WHERE id = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query, id).get(0);
            return new DeviceInfo(result.get("id").toString(), result.get("device").toString(), result.get("url").toString(),
                    result.get("name").toString(), result.get("imgurl").toString(), result.get("detail").toString(),
                    (Integer) result.get("price"), (Integer) result.get("rank"));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int delete(String id) {
        int number = jdbcTemplate.update("DELETE FROM devices WHERE id = ?", id);
        return number;
    }

    public int update(DeviceInfo deviceInfo) {
        int number = jdbcTemplate.update(
                "UPDATE devices SET device = ?, url = ?, name = ?, imgurl = ? , detail = ? , price = ? , rank = ? WHERE id = ?",
                deviceInfo.device(), deviceInfo.url(), deviceInfo.name(), deviceInfo.imgurl(), deviceInfo.detail(),
                deviceInfo.price(), deviceInfo.rank(), deviceInfo.id());
        return number;
    }


    // UserAssem DAO
    public void addUserAssem(UserAssem assem) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(assem);
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
                r.get("device").toString(), r.get("guestid").toString());
    }

    public List<UserAssem> findAllUserAssemByGuestId(String guestid) {
        String query = "SELECT * FROM assemblies WHERE guestid = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, guestid);
        List<UserAssem> assembliesList = result.stream().map(
                (Map<String, Object> row) -> new UserAssem(
                        row.get("id").toString(),
                        row.get("deviceid").toString(),
                        row.get("device").toString(),
                        row.get("guestid").toString()
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
}
