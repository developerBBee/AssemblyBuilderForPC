package jp.developer.bbee.pcassem.presentation.data

import jp.developer.bbee.pcassem.domain.model.SaveHead

@JvmRecord
data class SaveHeader(val url: String, val text: String) {
    companion object {
        @JvmField
        val CIRCLE_INDEX_5 = arrayOf("①", "②", "③", "④", "⑤")

        @JvmStatic
        fun create(sh: SaveHead, index: Int): SaveHeader =
            SaveHeader("/rec/${sh.saveid}", CIRCLE_INDEX_5[index])
    }
}
