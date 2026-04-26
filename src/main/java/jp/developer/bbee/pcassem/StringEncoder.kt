@file:JvmName("StringEncoder")

package jp.developer.bbee.pcassem

import java.io.UnsupportedEncodingException

@Throws(UnsupportedEncodingException::class)
fun sjisToUtf8(value: String): String {
    val sjis = charset("SJIS")
    var result = String(String(value.toByteArray(sjis), sjis).toByteArray(Charsets.UTF_8), Charsets.UTF_8)
    result = convert(result, "SJIS", "UTF-8")
    return result
}

@Throws(UnsupportedEncodingException::class)
fun utf8ToSjis(value: String): String {
    val sjis = charset("SJIS")
    var result = convert(String(value.toByteArray(Charsets.UTF_8), Charsets.UTF_8), "UTF-8", "SJIS")
    result = String(result.toByteArray(sjis), sjis)
    return result
}

@Throws(UnsupportedEncodingException::class)
private fun convert(value: String, src: String, dest: String): String {
    val conversion = createConversionMap(src, dest)
    return conversion.entries.fold(value) { acc, (key, v) ->
        acc.replace(toChar(key), toChar(v))
    }
}

@Throws(UnsupportedEncodingException::class)
private fun createConversionMap(src: String, dest: String): Map<String, String> = when {
    src == "UTF-8" && dest == "SJIS" -> mapOf(
        "U+FF0D" to "U+2212", // －（全角マイナス）
        "U+FF5E" to "U+301C", // ～（全角チルダ）
        "U+FFE0" to "U+00A2", // ￠（セント）
        "U+FFE1" to "U+00A3", // ￡（ポンド）
        "U+FFE2" to "U+00AC", // ￢（ノット）
        "U+2015" to "U+2014", // ―
        "U+2225" to "U+2016", // ∥
    )
    src == "SJIS" && dest == "UTF-8" -> mapOf(
        "U+2212" to "U+FF0D",
        "U+301C" to "U+FF5E",
        "U+00A2" to "U+FFE0",
        "U+00A3" to "U+FFE1",
        "U+00AC" to "U+FFE2",
        "U+2014" to "U+2015",
        "U+2016" to "U+2225",
    )
    else -> throw UnsupportedEncodingException("この文字コードはサポートしていません。\n・src=$src,dest=$dest")
}

private fun toChar(value: String): Char =
    Integer.parseInt(value.trim().substring("U+".length), 16).toChar()
