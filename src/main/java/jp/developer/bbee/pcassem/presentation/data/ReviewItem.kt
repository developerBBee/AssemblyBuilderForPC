package jp.developer.bbee.pcassem.presentation.data

import java.util.Locale

class ReviewItem {
    @JvmField var itemName: String? = null
    @JvmField var itemPrice: Int = 0

    override fun toString() = String.format(Locale.JAPAN, "%s(¥%,d)", itemName, itemPrice)
}
