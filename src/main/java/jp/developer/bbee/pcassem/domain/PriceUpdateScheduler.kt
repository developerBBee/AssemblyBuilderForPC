package jp.developer.bbee.pcassem.domain

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Timer
import java.util.TimerTask
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@ConditionalOnProperty(name = ["price-update.scheduler.enabled"], havingValue = "true", matchIfMissing = true)
class PriceUpdateScheduler(
    private val priceUpdateService: PriceUpdateService,
    private val dao: DeviceInfoDao,
) {
    private val logger = LoggerFactory.getLogger(PriceUpdateScheduler::class.java)
    private var fullUpdateDate = LocalDateTime.MIN
    private val timer = Timer("price-update-timer", /* isDaemon= */ true)

    @Volatile
    private var destroyed = false

    @PostConstruct
    fun init() {
        if (DEBUG) return
        Thread(::runTask).apply {
            name = "price-update-startup"
            isDaemon = true
            setUncaughtExceptionHandler { _, e ->
                logger.error("Uncaught exception in price update startup thread", e)
            }
        }.start()
    }

    @PreDestroy
    fun destroy() {
        destroyed = true
        timer.cancel()
    }

    private fun runTask() {
        try {
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
                    logger.warn("update kakaku failed", e)
                    loopCount++
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error in price update task", e)
        } finally {
            if (!destroyed) {
                val nextDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4, 0, 0))
                val delay = Duration.between(LocalDateTime.now(), nextDateTime).toMillis()
                try {
                    timer.schedule(object : TimerTask() {
                        override fun run() = runTask()
                    }, delay)
                    logger.info("Update scheduling, delay={}ms", delay)
                } catch (e: IllegalStateException) {
                    logger.info("Timer already cancelled during shutdown, skipping reschedule")
                }
            }
        }
    }

    companion object {
        private const val DEBUG = false
        private const val MAX_RETRY = 3
    }
}
