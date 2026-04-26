package jp.developer.bbee.pcassem.model

import java.time.LocalDateTime

@JvmRecord
data class UserAssem(
    val id: String,
    val deviceid: String,
    val device: String,
    val guestid: String,
    val createddate: LocalDateTime,
    val lastupdate: LocalDateTime,
)
