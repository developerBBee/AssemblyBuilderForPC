package jp.developer.bbee.pcassem

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class ApiResponseControllerTest {

    private val controller = ApiResponseController(mock(DeviceInfoDao::class.java))
    private val method = ApiResponseController::class.java
        .getDeclaredMethod("getUpdateMap", LocalDateTime::class.java)
        .also { it.isAccessible = true }

    @Test
    fun getUpdateMapTest() {
        val now = LocalDateTime.now()
        for (i in 0 until 1000) {
            val ldt = now.plusDays(i.toLong())

            @Suppress("UNCHECKED_CAST")
            val m = method.invoke(controller, ldt) as Map<String, Int>
            val ymd = m["kakakuupdate"]!!

            assertTrue(ymd >= 20230505)

            val year = ymd / 10000
            val month = ymd / 100 % 100
            val day = ymd % 100
            println("$i $ldt $ymd $year/$month/$day")

            assertTrue(year >= 2023)
            assertTrue(month in 1..12)

            when (month) {
                2 -> assertTrue(day in 1..29)
                4, 6, 9, 11 -> assertTrue(day in 1..30)
                else -> assertTrue(day in 1..31)
            }
        }
    }
}
