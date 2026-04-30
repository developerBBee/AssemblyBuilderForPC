package jp.developer.bbee.pcassem.presentation.controller

import jp.developer.bbee.pcassem.data.dao.DeviceInfoDao
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService
import jp.developer.bbee.pcassem.domain.model.DeviceInfo
import jp.developer.bbee.pcassem.domain.model.RestoreDevice
import jp.developer.bbee.pcassem.domain.model.SaveHead
import jp.developer.bbee.pcassem.domain.model.UserAssem
import jp.developer.bbee.pcassem.presentation.data.DeviceInfoFormatted
import jp.developer.bbee.pcassem.presentation.data.DeviceType
import jp.developer.bbee.pcassem.presentation.data.RestoreDeviceFormatted
import jp.developer.bbee.pcassem.presentation.data.SaveHeader
import jp.developer.bbee.pcassem.presentation.data.SaveRec
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class HomeController(
    private val dao: DeviceInfoDao,
    private val firestoreService: FirestoreService,
) {
    @ModelAttribute
    fun populateFirebaseUid(request: HttpServletRequest, model: Model) {
        val session = request.getSession(false) ?: return
        val uid = session.getAttribute("firebaseUid") as? String
        if (uid != null) {
            model.addAttribute("firebaseUid", uid)
        }
    }

    @GetMapping("/")
    fun top(model: Model, session: HttpSession): String {
        model.addAttribute("restoredListDisplay", "hidden")
        model.addAttribute("deviceListDisplay", "hidden")
        model.addAttribute("updateTime", dao.getTime().format(formatter))

        val firebaseUid = session.getAttribute("firebaseUid") as? String
        if (firebaseUid != null) {
            return topFromFirestore(model, firebaseUid)
        }

        model.addAttribute("assembliesDisplay", "hidden")
        model.addAttribute("saveHeadVisible", "hidden")
        return "index"
    }

    private fun topFromFirestore(model: Model, firebaseUid: String): String {
        try {
            // Save heads from Firestore
            val saveHeadList = firestoreService.getSaveHeadsRecent5(firebaseUid)
            if (saveHeadList.isNullOrEmpty()) {
                model.addAttribute("saveHeadVisible", "hidden")
            } else {
                val saveHeaderList = saveHeadList.mapIndexed { index, sh -> SaveHeader.create(sh, index) }
                model.addAttribute("saveHeaderList", saveHeaderList)
            }

            // Assemblies from Firestore
            val userAssems = firestoreService.getAssemblies(firebaseUid)
            val deviceIds = userAssems.map { it.deviceid }
            val deviceInfoList = dao.findRecordByIds(deviceIds)
            val assembliesList = sortList(deviceInfoList)

            val assemCountMap = HashMap<String?, Int>()
            for (di in assembliesList) {
                assemCountMap.merge(di.device, 1, Int::plus)
            }

            val formattedAssembliesList = makeFormattedList(assembliesList, assemCountMap)
            if (assembliesList.isEmpty()) {
                model.addAttribute("assembliesDisplay", "hidden")
            } else {
                model.addAttribute("assembliesList", formattedAssembliesList)
                var totalPrice = 0
                var isZeroPrice = false
                for (assembly in assembliesList) {
                    val price = assembly.price ?: 0
                    totalPrice += price
                    if (price == 0) isZeroPrice = true
                }
                model.addAttribute("totalPrice", DecimalFormat("¥ ###,###").format(totalPrice))
                if (!isZeroPrice) model.addAttribute("warnMsg1Visiblity", "hidden")
            }
        } catch (e: Exception) {
            logger.error(
                "[HomeController] Failed to load data from Firestore: {} - {}",
                e.javaClass.simpleName, e.message, e,
            )
            model.addAttribute("assembliesDisplay", "hidden")
            model.addAttribute("saveHeadVisible", "hidden")
        }
        return "index"
    }

    @GetMapping("/policy_ja")
    fun policy_ja(model: Model): String = "policy_ja"

    @GetMapping("/policy_en")
    fun policy_en(model: Model): String = "policy_en"

    @GetMapping("/home")
    fun home(model: Model): String = "index"

    @GetMapping("/pccase")
    fun pccase(model: Model): String {
        makeAttr(model, "pccase")
        return "index"
    }

    @GetMapping("/motherboard")
    fun motherboard(model: Model): String {
        makeAttr(model, "motherboard")
        return "index"
    }

    @GetMapping("/powersupply")
    fun powersupply(model: Model): String {
        makeAttr(model, "powersupply")
        return "index"
    }

    @GetMapping("/cpu")
    fun cpu(model: Model): String {
        makeAttr(model, "cpu")
        return "index"
    }

    @GetMapping("/cpucooler")
    fun cpucooler(model: Model): String {
        makeAttr(model, "cpucooler")
        return "index"
    }

    @GetMapping("/pcmemory")
    fun pcmemory(model: Model): String {
        makeAttr(model, "pcmemory")
        return "index"
    }

    @GetMapping("/storage")
    fun storage(model: Model): String {
        makeAttr(model, "storage", "ssd", "hdd35inch")
        return "index"
    }

    @GetMapping("/videocard")
    fun videocard(model: Model): String {
        makeAttr(model, "videocard")
        return "index"
    }

    @GetMapping("/ossoft")
    fun ossoft(model: Model): String {
        makeAttr(model, "ossoft")
        return "index"
    }

    @GetMapping("/lcdmonitor")
    fun lcdmonitor(model: Model): String {
        makeAttr(model, "lcdmonitor")
        return "index"
    }

    @GetMapping("/keyboard")
    fun keyboard(model: Model): String {
        makeAttr(model, "keyboard")
        return "index"
    }

    @GetMapping("/mouse")
    fun mouse(model: Model): String {
        makeAttr(model, "mouse")
        return "index"
    }

    @GetMapping("/mediadrive")
    fun mediadrive(model: Model): String {
        makeAttr(model, "mediadrive", "bluraydrive", "dvddrive")
        return "index"
    }

    @GetMapping("/soundcard")
    fun soundcard(model: Model): String {
        makeAttr(model, "soundcard")
        return "index"
    }

    @GetMapping("/pcspeaker")
    fun pcspeaker(model: Model): String {
        makeAttr(model, "pcspeaker")
        return "index"
    }

    @GetMapping("/fancontroller")
    fun fancontroller(model: Model): String {
        makeAttr(model, "fancontroller")
        return "index"
    }

    @GetMapping("/casefan")
    fun casefan(model: Model): String {
        makeAttr(model, "casefan")
        return "index"
    }

    private fun makeAttr(model: Model, deviceName: String) {
        makeAttr(model, deviceName, deviceName, null)
    }

    private fun makeAttr(model: Model, deviceTypeName: String, deviceName1: String, deviceName2: String?) {
        val s = (model.getAttribute("sortFlag") as? Int) ?: 0
        var deviceInfoList = dao.findAll(deviceName1, s)
        deviceInfoList = noPriceAfter(deviceInfoList)
        val formattedList = makeFormattedList(deviceInfoList).toMutableList()

        if (deviceName2 != null) {
            val deviceInfoList2 = noPriceAfter(dao.findAll(deviceName2, s))
            formattedList.addAll(makeFormattedList(deviceInfoList2))
        }

        val firebaseUid = model.getAttribute("firebaseUid") as? String
        if (firebaseUid != null) {
            try {
                val userAssems = firestoreService.getAssemblies(firebaseUid)
                val registeredIds = userAssems.map { it.deviceid }.toSet()
                for (i in formattedList.indices) {
                    val dif = formattedList[i]
                    if (registeredIds.contains(dif.id)) {
                        formattedList[i] = DeviceInfoFormatted(
                            dif.id, dif.device, dif.url, dif.name, dif.imgurl, dif.detail, dif.price,
                            dif.rank, true, "middle", 1, false, dif.flag1, dif.flag2,
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(
                    "[HomeController] Failed to get assemblies from Firestore: {} - {}",
                    e.javaClass.simpleName, e.message, e,
                )
            }
        }

        model.addAttribute("saveHeadVisible", "hidden")
        model.addAttribute("assembliesDisplay", "hidden")
        model.addAttribute("restoredListDisplay", "hidden")
        model.addAttribute("deviceInfoList", formattedList)
        model.addAttribute("deviceTypeName", deviceTypeName)
        model.addAttribute("sortFlag", s)
        model.addAttribute("updateTime", dao.getTime().format(formatter))
    }

    private fun makeFormattedList(deviceInfoList: List<DeviceInfo>): List<DeviceInfoFormatted> =
        deviceInfoList.map { di ->
            DeviceInfoFormatted(
                id = di.id,
                device = DeviceType.JP_MAP[di.device],
                url = di.url,
                name = di.name,
                imgurl = di.imgurl,
                detail = di.detail,
                price = formatPrice(di.price),
                rank = di.rank?.toString() ?: "0",
                registered = false,
                tablestyle = "middle",
                rowspan = 1,
                checked = false,
                flag1 = di.flag1,
                flag2 = di.flag2,
            )
        }

    private fun makeFormattedList(
        deviceInfoList: List<DeviceInfo>,
        countMap: Map<String?, Int>,
    ): List<DeviceInfoFormatted> {
        val formattedList = mutableListOf<DeviceInfoFormatted>()
        var deviceCount = 0
        for (di in deviceInfoList) {
            val count = countMap[di.device] ?: 0
            val tableStyle: String
            val rowSpan: Int
            val checked: Boolean
            if (deviceCount == 0) {
                tableStyle = if (deviceCount == count - 1) "" else "top"
                rowSpan = count
                checked = true
            } else if (deviceCount == count - 1) {
                tableStyle = "bottom"
                rowSpan = 1
                checked = false
            } else {
                tableStyle = "middle"
                rowSpan = 1
                checked = false
            }

            formattedList.add(
                DeviceInfoFormatted(
                    id = di.id,
                    device = DeviceType.JP_MAP[di.device],
                    url = di.url,
                    name = di.name,
                    imgurl = di.imgurl,
                    detail = di.detail,
                    price = formatPrice(di.price),
                    rank = di.rank?.toString() ?: "0",
                    registered = false,
                    tablestyle = tableStyle,
                    rowspan = rowSpan,
                    checked = checked,
                    flag1 = di.flag1,
                    flag2 = di.flag2,
                )
            )
            deviceCount = if (deviceCount == count - 1) 0 else deviceCount + 1
        }
        return formattedList
    }

    private fun formatPrice(price: Int?): String =
        if (price == null || price == 0) "価格情報なし" else DecimalFormat("¥ ###,###").format(price)

    private fun sortList(deviceInfoList: List<DeviceInfo>): List<DeviceInfo> {
        val sortedList = mutableListOf<DeviceInfo>()
        for (dev in DeviceType.LIST) {
            for (di in deviceInfoList) {
                if (dev == di.device) sortedList.add(di)
            }
        }
        return sortedList
    }

    private fun noPriceAfter(list: List<DeviceInfo>): List<DeviceInfo> {
        val retList = list.toMutableList()
        for (l in list) {
            if (l.price == null || l.price == 0) {
                retList.removeAt(0)
                retList.add(l)
            } else {
                break
            }
        }
        return retList
    }

    @PostMapping("/add") // Add device to assemblies
    fun addUserAssem(
        redirectAttributes: RedirectAttributes,
        @RequestParam("id") id: String,
        @RequestParam("devType") deviceTypeName: String,
        @RequestParam("body_scroll_px") bodyScrollPx: String,
        @RequestParam("sortFlag") sortFlag: String,
        session: HttpSession,
    ): String {
        val firebaseUid = session.getAttribute("firebaseUid") as? String
            ?: return "redirect:/$deviceTypeName"

        val di = dao.findRecordById(id)
        if (di == null) {
            logger.error("[HomeController] Device not found for id: {}", id)
            return "redirect:/$deviceTypeName"
        }
        val assem = UserAssem(
            UUID.randomUUID().toString().replace("-", ""),
            di.id!!, di.device!!, firebaseUid,
            LocalDateTime.now(), LocalDateTime.now(),
        )
        try {
            firestoreService.addAssembly(firebaseUid, assem)
        } catch (e: Exception) {
            logger.error(
                "[HomeController] Failed to add assembly to Firestore: {} - {}",
                e.javaClass.simpleName, e.message, e,
            )
        }

        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx)
        redirectAttributes.addFlashAttribute("sortFlag", sortFlag.toIntOrNull() ?: 0)
        return "redirect:/$deviceTypeName"
    }

    @PostMapping("/del") // Delete device from assemblies
    fun delUserAssem(
        redirectAttributes: RedirectAttributes,
        @RequestParam("id") id: String,
        @RequestParam("devType") deviceTypeName: String,
        @RequestParam("body_scroll_px") bodyScrollPx: String,
        session: HttpSession,
    ): String {
        val firebaseUid = session.getAttribute("firebaseUid") as? String
            ?: return "redirect:/"

        try {
            firestoreService.deleteAssembly(firebaseUid, id)
        } catch (e: Exception) {
            logger.error(
                "[HomeController] Failed to delete assembly from Firestore: {} - {}",
                e.javaClass.simpleName, e.message, e,
            )
        }

        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx)
        return "redirect:/"
    }

    @GetMapping("/sort") // Sort devices
    fun sortDevices(
        redirectAttributes: RedirectAttributes,
        @RequestParam("sort") sort: String,
        @RequestParam("devType") deviceTypeName: String,
        @RequestParam("body_scroll_px") bodyScrollPx: String,
    ): String {
        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx)
        redirectAttributes.addFlashAttribute("sortFlag", sortMap[sort])
        return "redirect:/$deviceTypeName"
    }

    @PostMapping("/save") // Save assemblies of user's construction.
    fun saveConstruction(saveRec: SaveRec, session: HttpSession): String {
        val firebaseUid = session.getAttribute("firebaseUid") as? String
        val deviceIdList = saveRec.deviceIdList
        if (deviceIdList.isNullOrEmpty() || firebaseUid == null) {
            return "redirect:/"
        }
        val uuid = UUID.randomUUID().toString().replace("-", "")
        dao.save(uuid, firebaseUid, deviceIdList)

        try {
            val items = dao.getSaveItemsBySaveId(uuid)
            val saveHead = SaveHead(uuid, firebaseUid, "NONAME", LocalDateTime.now(), LocalDateTime.now())
            firestoreService.saveSaves(firebaseUid, null, listOf(saveHead), mapOf(uuid to items))
        } catch (e: Exception) {
            logger.error(
                "[HomeController] Failed to save to Firestore: {} - {}",
                e.javaClass.simpleName, e.message, e,
            )
        }

        return "redirect:/rec/$uuid"
    }

    @GetMapping("/rec/{saveId:[0-9a-fA-F]{32}}")
    fun restoreConstruction(model: Model, @PathVariable saveId: String): String {
        val normalizedSaveId = saveId.lowercase()

        // Firestore から取得を試みる。例外時は即 503。
        val saveItems = try {
            firestoreService.getSaveItems(normalizedSaveId)
        } catch (e: Exception) {
            logger.error(
                "[HomeController] Failed to get save from Firestore: {} - {}",
                e.javaClass.simpleName, e.message, e,
            )
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)
        }

        // ドキュメントが存在する場合はその結果を使う（items が空でも H2 にフォールバックしない）
        // ドキュメントが存在しない場合のみ H2 から取得
        val rdList: List<RestoreDevice> = if (saveItems.isPresent) {
            buildRestoreDevicesFromSaveItems(normalizedSaveId, saveItems.get())
        } else {
            dao.restore(normalizedSaveId)
        }

        if (rdList.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val rdfList = rdList.map { RestoreDeviceFormatted.create(it) }
        model.addAttribute("saveHeadVisible", "hidden")
        model.addAttribute("restoredList", rdfList)
        model.addAttribute("assembliesDisplay", "hidden")
        model.addAttribute("deviceListDisplay", "hidden")
        model.addAttribute("updateTime", dao.getTime().format(formatter))
        return "index"
    }

    private fun buildRestoreDevicesFromSaveItems(
        saveId: String,
        saveItems: List<DeviceInfoDao.SaveItem>,
    ): List<RestoreDevice> {
        val deviceIds = saveItems
            .mapNotNull { it.deviceId }
            .filter { it.isNotBlank() }
            .distinct()
        val deviceMap = dao.findRecordByIds(deviceIds).associateBy { it.id }
        return saveItems
            .filter { deviceMap.containsKey(it.deviceId) }
            .map { item ->
                val di = deviceMap[item.deviceId]!!
                RestoreDevice(
                    saveId, di.id!!, di.device!!, di.url!!, di.name!!,
                    di.imgurl!!, di.detail!!, item.price, di.price,
                )
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HomeController::class.java)
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm")
        private val sortMap = mapOf(
            "popular" to 0,
            "lower" to 1,
            "higher" to 2,
            "newer" to 3,
        )
    }
}
