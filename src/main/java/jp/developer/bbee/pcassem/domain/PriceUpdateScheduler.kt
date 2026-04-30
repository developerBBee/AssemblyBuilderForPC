package jp.developer.bbee.pcassem.domain

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Timer
import java.util.TimerTask
import javax.annotation.PostConstruct

@Component
class PriceUpdateScheduler(
    private val priceUpdateService: PriceUpdateService,
    private val dao: DeviceInfoDao,
) {
    private val logger = LoggerFactory.getLogger(PriceUpdateScheduler::class.java)
    private var fullUpdateDate = LocalDateTime.MIN

    @PostConstruct
    fun init() {
        if (DEBUG) return
        Thread(::runTask).start()
    }

    private fun runTask() {
        val fullUpdate = Duration.between(fullUpdateDate, LocalDateTime.now()).toHours() > 165
        priceUpdateService.prepare(fullUpdate)

        var incomplete = true
        var loopCount = 0
        while (incomplete && loopCount <= MAX_RETRY) {
            try {
                priceUpdateService.execute()
                incomplete = false
                val now = LocalDateTime.now()
                if (fullUpdate) fullUpdateDate = now
                dao.setTime(now)
            } catch (e: IOException) {
                logger.warn("update kakaku failed. reason={}", e.message)
                loopCount++
            }
        }

        val nextDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4, 0, 0))
        val delay = Duration.between(LocalDateTime.now(), nextDateTime).toMillis()
        Timer().schedule(object : TimerTask() {
            override fun run() = runTask()
        }, delay)
        logger.info("Update scheduling, delay={}ms", delay)
    }

    companion object {
        private const val DEBUG = false
        private const val MAX_RETRY = 3
    }
}
