package jp.developer.bbee.pcassem.domain

import java.time.LocalDateTime

object DateTimeConst {
    @JvmField
    val FALLBACK: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0)
}
