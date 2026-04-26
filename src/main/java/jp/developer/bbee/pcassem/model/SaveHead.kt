package jp.developer.bbee.pcassem.model

import java.time.LocalDateTime

@JvmRecord
data class SaveHead(
    val saveid: String,
    val guestid: String,
    val savename: String,
    val createddate: LocalDateTime,
    val lastupdate: LocalDateTime,
)
