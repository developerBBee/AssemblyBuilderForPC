package jp.developer.bbee.pcassem.presentation.controller

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao
import jp.developer.bbee.pcassem.domain.model.DeviceInfo
import jp.developer.bbee.pcassem.presentation.ApiEndPoint
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("api/")
class ApiResponseController(private val dao: DeviceInfoDao) {

    @GetMapping(ApiEndPoint.GET_DEVICE)
    fun getDeviceList(@RequestParam(value = "device", defaultValue = "pccase") device: String): Map<String, List<DeviceInfo>> =
        mapOf("results" to dao.findAll(device))

    @GetMapping(ApiEndPoint.GET_UPDATE)
    fun getLastUpdate(): Map<String, Int> = getUpdateMap(dao.getTime())

    private fun getUpdateMap(ldt: LocalDateTime): Map<String, Int> =
        mapOf("kakakuupdate" to (ldt.year * 10000 + ldt.monthValue * 100 + ldt.dayOfMonth))
}
