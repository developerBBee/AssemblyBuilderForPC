package jp.developer.bbee.pcassem.presentation.data

@JvmRecord
data class DeviceInfoFormatted(
    val id: String?,
    val device: String?,
    val url: String?,
    val name: String?,
    val imgurl: String?,
    val detail: String?,
    val price: String,
    val rank: String,
    val registered: Boolean,
    val tablestyle: String,
    val rowspan: Int,
    val checked: Boolean,
    val flag1: Int,
    val flag2: Int,
)
