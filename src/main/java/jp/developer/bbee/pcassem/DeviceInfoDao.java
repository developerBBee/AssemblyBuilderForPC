package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.HomeController.DeviceInfo;
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

    public void add(DeviceInfo deviceInfo, String tableName) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(deviceInfo);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(tableName);
        insert.execute(param);
    }

    public List<DeviceInfo> findAll(String tableName) {
        String query = "SELECT * FROM " + tableName;
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

        List<DeviceInfo> deviceInfoList = result.stream().map(
                (Map<String, Object> row) -> new DeviceInfo(
                        row.get("id") != null ? row.get("id").toString() : UUID.randomUUID().toString().replace("-", ""),
                        row.get("url") != null ? row.get("url").toString() : "",
                        row.get("name") != null ? row.get("name").toString() : "",
                        row.get("imgurl") != null ? row.get("imgurl").toString() : "",
                        row.get("type") != null ? row.get("type").toString() : "",
                        row.get("price") != null ? (Integer) row.get("price") : 0,
                        row.get("rank") != null ? (Integer) row.get("rank") : 99
                )).toList();

        return deviceInfoList;
    }

    public DeviceInfo findRecordByUrl(String url, String tableName) {
        String query = "SELECT * FROM " + tableName + " WHERE url = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForList(query, url).get(0);
            return new DeviceInfo(result.get("id").toString(), result.get("url").toString(), result.get("name").toString(),
                    result.get("imgurl").toString(), result.get("type").toString(),
                    (Integer) result.get("price"), (Integer) result.get("rank"));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int delete(String id, String tableName) {
        int number = jdbcTemplate.update("DELETE FROM " + tableName + " WHERE id = ?", id);
        return number;
    }

    public int update(DeviceInfo deviceInfo, String tableName) {
        int number = jdbcTemplate.update(
                "UPDATE " + tableName + " SET url = ?, name = ?, imgurl = ? , type = ? , price = ? , rank = ? WHERE id = ?",
                deviceInfo.url(), deviceInfo.name(), deviceInfo.imgurl(), deviceInfo.type(),
                deviceInfo.price(), deviceInfo.rank(), deviceInfo.id());
        return number;
    }
}
