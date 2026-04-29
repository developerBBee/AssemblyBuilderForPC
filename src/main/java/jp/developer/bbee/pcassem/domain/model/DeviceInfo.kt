package jp.developer.bbee.pcassem.domain.model

import java.sql.Timestamp
import java.time.LocalDateTime

@JvmRecord
data class DeviceInfo(
    val id: String?,
    val device: String?,
    val url: String?,
    val name: String?,
    val imgurl: String?,
    val detail: String?,
    val price: Int?,
    val rank: Int?,
    val flag1: Int,
    val flag2: Int,
    val releasedate: String?,
    val invisible: Int?,
    val createddate: LocalDateTime,
    val lastupdate: LocalDateTime,
) {
    companion object {
        private const val DEFAULT_RELEASE_DATE = "20000101"
        private val DEFAULT_DATE_TIME = LocalDateTime.of(2000, 1, 1, 0, 0)

        @JvmStatic
        fun from(result: Map<String, Any?>): DeviceInfo = DeviceInfo(
            id = result["id"] as? String,
            device = result["device"] as? String,
            url = result["url"] as? String,
            name = result["name"] as? String,
            imgurl = result["imgurl"] as? String,
            detail = result["detail"] as? String,
            price = toInteger(result["price"]),
            rank = toInteger(result["rank"]),
            flag1 = toIntOrDefault(result["flag1"], 0),
            flag2 = toIntOrDefault(result["flag2"], 0),
            releasedate = toReleaseDate(result["releasedate"]),
            invisible = toInteger(result["invisible"]),
            createddate = toLocalDateTime(result["createddate"], DEFAULT_DATE_TIME),
            lastupdate = toLocalDateTime(result["lastupdate"], DEFAULT_DATE_TIME),
        )

        private fun toInteger(value: Any?): Int? = when {
            value == null -> null
            value is Int -> value
            value is Number -> value.toInt()
            else -> value.toString().toInt()
        }

        private fun toIntOrDefault(value: Any?, defaultValue: Int): Int =
            toInteger(value) ?: defaultValue

        private fun toReleaseDate(value: Any?): String =
            value?.toString() ?: DEFAULT_RELEASE_DATE

        private fun toLocalDateTime(value: Any?, defaultValue: LocalDateTime): LocalDateTime = when {
            value == null -> defaultValue
            value is LocalDateTime -> value
            value is Timestamp -> value.toLocalDateTime()
            value is java.util.Date -> Timestamp(value.time).toLocalDateTime()
            else -> Timestamp.valueOf(value.toString()).toLocalDateTime()
        }
    }
}
