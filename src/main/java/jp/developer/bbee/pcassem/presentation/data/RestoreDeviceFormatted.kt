package jp.developer.bbee.pcassem.presentation.data

import jp.developer.bbee.pcassem.domain.model.RestoreDevice
import java.text.DecimalFormat

@JvmRecord
data class RestoreDeviceFormatted(
    val saveid: String,
    val deviceid: String,
    val device: String,
    val url: String,
    val name: String,
    val imgurl: String,
    val detail: String,
    val oldprice: String,
    val newprice: String,
    val diffprice: String,
    val color: String,
) {
    companion object {
        @JvmStatic
        fun create(rd: RestoreDevice): RestoreDeviceFormatted {
            val op = rd.oldprice ?: 0
            val np = rd.newprice ?: 0
            return RestoreDeviceFormatted(
                saveid = rd.saveid,
                deviceid = rd.deviceid,
                device = rd.device,
                url = rd.url,
                name = rd.name,
                imgurl = rd.imgurl,
                detail = rd.detail,
                oldprice = if (op == 0) "価格情報なし" else DecimalFormat("¥ ###,###").format(op),
                newprice = if (np == 0) "価格情報なし" else DecimalFormat("¥ ###,###").format(np),
                diffprice = if (np == 0 || op == 0) "" else
                    DecimalFormat("(+###,###);(-###,###)").format(np - op).replace("(+0)", "(±0)"),
                color = when {
                    np == op -> "black"
                    np > op -> "red"
                    else -> "blue"
                },
            )
        }
    }
}
