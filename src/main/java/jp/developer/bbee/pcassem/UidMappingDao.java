package jp.developer.bbee.pcassem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UidMappingDao {

    private final JdbcTemplate jdbcTemplate;

    public record UidMapping(String firebaseUid, String guestId, LocalDateTime migratedAt) {}

    @Autowired
    UidMappingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UidMapping findByFirebaseUid(String firebaseUid) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM uid_mapping WHERE firebase_uid = ?", firebaseUid);
        if (result.isEmpty()) return null;
        return toRecord(result.get(0));
    }

    public UidMapping findByGuestId(String guestId) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM uid_mapping WHERE guest_id = ?", guestId);
        if (result.isEmpty()) return null;
        return toRecord(result.get(0));
    }

    public void insert(String firebaseUid, String guestId) {
        jdbcTemplate.update(
                "INSERT INTO uid_mapping (firebase_uid, guest_id, migrated_at) VALUES (?, ?, ?)",
                firebaseUid, guestId, Timestamp.valueOf(LocalDateTime.now()));
    }

    private UidMapping toRecord(Map<String, Object> r) {
        Timestamp ts = (Timestamp) r.get("migrated_at");
        return new UidMapping(
                r.get("firebase_uid").toString(),
                r.get("guest_id").toString(),
                ts != null ? ts.toLocalDateTime() : null);
    }
}
