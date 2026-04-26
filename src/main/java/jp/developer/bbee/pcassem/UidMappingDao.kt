package jp.developer.bbee.pcassem

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
class UidMappingDao(private val jdbcTemplate: JdbcTemplate) {

    data class UidMapping(val firebaseUid: String, val guestId: String?, val migratedAt: LocalDateTime?)

    fun findByFirebaseUid(firebaseUid: String): UidMapping? {
        val result = jdbcTemplate.queryForList("SELECT * FROM uid_mapping WHERE firebase_uid = ?", firebaseUid)
        return result.firstOrNull()?.let { toRecord(it) }
    }

    fun findByGuestId(guestId: String): UidMapping? {
        val result = jdbcTemplate.queryForList("SELECT * FROM uid_mapping WHERE guest_id = ?", guestId)
        return result.firstOrNull()?.let { toRecord(it) }
    }

    fun insert(firebaseUid: String, guestId: String) {
        jdbcTemplate.update(
            "INSERT INTO uid_mapping (firebase_uid, guest_id, migrated_at) VALUES (?, ?, ?)",
            firebaseUid, guestId, Timestamp.valueOf(LocalDateTime.now()),
        )
    }

    private fun toRecord(r: Map<String, Any?>): UidMapping {
        val ts = r["migrated_at"] as? Timestamp
        return UidMapping(
            firebaseUid = r["firebase_uid"].toString(),
            guestId = r["guest_id"]?.toString(),
            migratedAt = ts?.toLocalDateTime(),
        )
    }
}
