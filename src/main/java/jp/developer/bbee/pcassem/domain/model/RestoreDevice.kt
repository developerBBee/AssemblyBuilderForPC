package jp.developer.bbee.pcassem.domain.model

@JvmRecord
data class RestoreDevice(
    val saveid: String,
    val deviceid: String,
    val device: String,
    val url: String,
    val name: String,
    val imgurl: String,
    val detail: String,
    val oldprice: Int?,
    val newprice: Int?,
)
