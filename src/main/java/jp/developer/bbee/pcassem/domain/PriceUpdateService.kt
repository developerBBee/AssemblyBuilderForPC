package jp.developer.bbee.pcassem.domain

import java.io.IOException

interface PriceUpdateService {
    fun prepare(fullUpdate: Boolean)

    @Throws(IOException::class)
    fun execute()
}
