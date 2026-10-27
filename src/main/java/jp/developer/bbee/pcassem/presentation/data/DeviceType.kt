package jp.developer.bbee.pcassem.presentation.data

object DeviceType {
    @JvmField
    val LIST = listOf(
        "pccase", "motherboard", "powersupply", "cpu", "cpucooler", "pcmemory",
        "hdd35inch", "ssd", "videocard", "ossoft", "lcdmonitor", "keyboard",
        "mouse", "dvddrive", "bluraydrive", "soundcard", "pcspeaker", "fancontroller", "casefan",
    )

    @JvmField
    val JP_MAP: Map<String, String> = mapOf(
        "pccase"        to "PCケース",
        "motherboard"   to "マザーボード",
        "powersupply"   to "電源",
        "cpu"           to "CPU",
        "cpucooler"     to "CPUクーラー",
        "pcmemory"      to "メモリ",
        "hdd35inch"     to "HDD",
        "ssd"           to "SSD",
        "videocard"     to "グラフィックボード",
        "ossoft"        to "OS",
        "lcdmonitor"    to "ディスプレイ",
        "keyboard"      to "キーボード",
        "mouse"         to "マウス",
        "dvddrive"      to "DVDドライブ",
        "bluraydrive"   to "BDドライブ",
        "soundcard"     to "サウンドカード",
        "pcspeaker"     to "スピーカー",
        "fancontroller" to "ファンコントローラー",
        "casefan"       to "ファン",
    )
}
