package jp.developer.bbee.pcassem;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService;
import jp.developer.bbee.pcassem.model.DeviceInfo;
import jp.developer.bbee.pcassem.model.SaveHead;
import jp.developer.bbee.pcassem.model.UserAssem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.TimerTask;
import java.util.UUID;

@Controller
public class HomeController {
    public static final boolean DEBUG = false;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm");
    private static final int MAX_RETRY = 3;
    private final DeviceInfoDao dao;
    private final FirestoreService firestoreService;
    private final KakakuClient kakakuClient;

    private LocalDateTime fullUpdateDate = LocalDateTime.MIN;

    public Map<String, String> deviceTypeJp = new HashMap<>();

    public List<String> deviceTypeList = List.of(
            "pccase", "motherboard", "powersupply", "cpu", "cpucooler", "pcmemory", "hdd35inch", "ssd", "videocard",
            "ossoft", "lcdmonitor", "keyboard", "mouse", "dvddrive", "bluraydrive", "soundcard", "pcspeaker", "fancontroller", "casefan"
            );

    @Autowired // <- DAO auto setting
    HomeController(DeviceInfoDao dao, FirestoreService firestoreService){
        this.dao = dao;
        this.firestoreService = firestoreService;
        kakakuClient = new KakakuClient(dao);
        makeDeviceTypeJp();
    }

    @ModelAttribute
    public void populateFirebaseUid(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        String uid = (String) session.getAttribute("firebaseUid");
        if (uid != null) {
            model.addAttribute("firebaseUid", uid);
        }
    }

    @PostConstruct
    void init() {
        updateKakaku();
    }

    private void makeDeviceTypeJp() {
        deviceTypeJp.put("pccase", "PCケース"); // PC case
        deviceTypeJp.put("motherboard", "マザーボード"); // Motherboard
        deviceTypeJp.put("powersupply", "電源"); // Power supply unit
        deviceTypeJp.put("cpu", "CPU"); // CPU
        deviceTypeJp.put("cpucooler", "CPUクーラー"); // CPU cooler
        deviceTypeJp.put("pcmemory", "メモリ"); // Memory
        deviceTypeJp.put("hdd35inch", "HDD"); // Storage HDD
        deviceTypeJp.put("ssd", "SSD"); // Storage SSD
        deviceTypeJp.put("videocard", "グラフィックボード"); // Graphic board
        deviceTypeJp.put("ossoft", "OS"); // OS soft
        deviceTypeJp.put("lcdmonitor", "ディスプレイ"); // Display
        deviceTypeJp.put("keyboard", "キーボード"); // Keyboard
        deviceTypeJp.put("mouse", "マウス"); // Mouse
        deviceTypeJp.put("dvddrive", "DVDドライブ"); // DVD media drive
        deviceTypeJp.put("bluraydrive", "BDドライブ"); // Blue-rya media drive
        deviceTypeJp.put("soundcard", "サウンドカード"); // Sound card
        deviceTypeJp.put("pcspeaker", "スピーカー"); // Speaker
        deviceTypeJp.put("fancontroller", "ファンコントローラー"); // Fan controller
        deviceTypeJp.put("casefan", "ファン"); // Case fan
    }

    private void updateKakaku() {

        if (DEBUG) return;
        new Thread(this::runTask).start(); // Run task at startup

    }

    public void runTask() {
        boolean incomplete = true;
        boolean fullUpdate = (Duration.between(fullUpdateDate, LocalDateTime.now()).toHours() > 165); // 24*7=168
//        fullUpdate = true; // debug
        kakakuClient.unAcquired = fullUpdate;

        int loopCount = 0;
        while (incomplete && loopCount <= MAX_RETRY) {
            try {
                if (kakakuClient.unAcquired && fullUpdate) {
                    kakakuClient.getKakaku();
                } else {
                    kakakuClient.updateKakaku(false);
                }
                incomplete = false;
                LocalDateTime lastUpdateDate = LocalDateTime.now();
                if (fullUpdate) fullUpdateDate = lastUpdateDate;
                dao.setTime(lastUpdateDate);
            } catch (IOException e) {
                System.out.println("update kakaku failed. reason=" + e.getMessage());
                loopCount++;
            }
        }

        Timer timer = new Timer();
        TimerTask task = new MyTimerTask(HomeController.this);
        LocalDateTime nextDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4,0,0));
        long delay = Duration.between(LocalDateTime.now(), nextDateTime).toMillis();
//        delay = 300000; // debug
        timer.schedule(task, delay); // Run task on schedule
        System.out.println("Update scheduling, delay=" + delay + "ms");
    }

    static class MyTimerTask extends TimerTask {
        private final HomeController controller;
        MyTimerTask(HomeController hc) {
            this.controller = hc;
        }
        @Override
        public void run() {
            controller.runTask();
        }
    }

    record DeviceInfoFormatted (String id, String device, String url, String name, String imgurl, String detail, String price, String rank, boolean registered,
                                String tablestyle, int rowspan, boolean checked, int flag1, int flag2) {}

    record SaveHeader (String url, String text) {
        static SaveHeader create(SaveHead sh, int index) {
            return new SaveHeader("/rec/"+ sh.saveid(), CIRCLE_INDEX_5[index]);
        }
    }
    static final String[] CIRCLE_INDEX_5 = {"①", "②", "③", "④", "⑤"};

    @GetMapping("/")
    String top(Model model, HttpSession session) {
        model.addAttribute("restoredListDisplay", "hidden");
        model.addAttribute("deviceListDisplay", "hidden");
        model.addAttribute("updateTime", dao.getTime().format(formatter));

        String firebaseUid = (String) session.getAttribute("firebaseUid");
        if (firebaseUid != null) {
            return topFromFirestore(model, firebaseUid);
        }

        model.addAttribute("assembliesDisplay", "hidden");
        model.addAttribute("saveHeadVisible", "hidden");
        return "index";
    }

    private String topFromFirestore(Model model, String firebaseUid) {
        try {
            // Save heads from Firestore
            List<SaveHead> saveHeadList = firestoreService.getSaveHeadsRecent5(firebaseUid);
            if (saveHeadList == null || saveHeadList.isEmpty()) {
                model.addAttribute("saveHeadVisible", "hidden");
            } else {
                List<SaveHeader> saveHeaderList = new ArrayList<>();
                int index = 0;
                for (SaveHead sh : saveHeadList) {
                    saveHeaderList.add(SaveHeader.create(sh, index));
                    index++;
                }
                model.addAttribute("saveHeaderList", saveHeaderList);
            }

            // Assemblies from Firestore
            List<UserAssem> userAssems = firestoreService.getAssemblies(firebaseUid);
            List<String> deviceIds = userAssems.stream().map(UserAssem::deviceid).toList();
            var deviceInfoList = dao.findRecordByIds(deviceIds);
            List<DeviceInfo> assembliesList = sortList(deviceInfoList);

            Map<String, Integer> assemCountMap = new HashMap<>();
            for (DeviceInfo di : assembliesList) {
                assemCountMap.merge(di.device(), 1, Integer::sum);
            }

            List<DeviceInfoFormatted> formattedAssembliesList = makeFormattedList(assembliesList, assemCountMap);
            if (assembliesList.isEmpty()) {
                model.addAttribute("assembliesDisplay", "hidden");
            } else {
                model.addAttribute("assembliesList", formattedAssembliesList);
                int totalPrice = 0;
                boolean isZeroPrice = false;
                for (DeviceInfo assembly : assembliesList) {
                    totalPrice += assembly.price();
                    if (assembly.price() == 0) isZeroPrice = true;
                }
                model.addAttribute("totalPrice", new DecimalFormat("¥ ###,###").format(totalPrice));
                if (!isZeroPrice) model.addAttribute("warnMsg1Visiblity", "hidden");
            }
        } catch (Exception e) {
            logger.error("[HomeController] Failed to load data from Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            model.addAttribute("assembliesDisplay", "hidden");
            model.addAttribute("saveHeadVisible", "hidden");
        }
        return "index";
    }

    @GetMapping("/policy_ja")
    String policy_ja(Model model) {
        return "policy_ja";
    }
    @GetMapping("/policy_en")
    String policy_en(Model model) {
        return "policy_en";
    }

    @GetMapping("/home")
    String home(Model model) {
        return "index";
    }

    @GetMapping("/pccase")
    String pccase(Model model) {
        makeAttr(model,"pccase");
        return "index";
    }

    @GetMapping("/motherboard")
    String motherboard(Model model) {
        makeAttr(model,"motherboard");
        return "index";
    }

    @GetMapping("/powersupply")
    String powersupply(Model model) {
        makeAttr(model,"powersupply");
        return "index";
    }

    @GetMapping("/cpu")
    String cpu(Model model) {
        makeAttr(model,"cpu");
        return "index";
    }

    @GetMapping("/cpucooler")
    String cpucooler(Model model) {
        makeAttr(model,"cpucooler");
        return "index";
    }

    @GetMapping("/pcmemory")
    String pcmemory(Model model) {
        makeAttr(model,"pcmemory");
        return "index";
    }

    @GetMapping("/storage")
    String storage(Model model) {
        makeAttr(model, "storage", "ssd", "hdd35inch");
        return "index";
    }

    @GetMapping("/videocard")
    String videocard(Model model) {
        makeAttr(model,"videocard");
        return "index";
    }

    @GetMapping("/ossoft")
    String ossoft(Model model) {
        makeAttr(model,"ossoft");
        return "index";
    }

    @GetMapping("/lcdmonitor")
    String lcdmonitor(Model model) {
        makeAttr(model,"lcdmonitor");
        return "index";
    }

    @GetMapping("/keyboard")
    String keyboard(Model model) {
        makeAttr(model,"keyboard");
        return "index";
    }

    @GetMapping("/mouse")
    String mouse(Model model) {
        makeAttr(model,"mouse");
        return "index";
    }

    @GetMapping("/mediadrive")
    String mediadrive(Model model) {
        makeAttr(model,  "mediadrive", "bluraydrive", "dvddrive");
        return "index";
    }

    @GetMapping("/soundcard")
    String soundcard(Model model) {
        makeAttr(model,"soundcard");
        return "index";
    }

    @GetMapping("/pcspeaker")
    String pcspeaker(Model model) {
        makeAttr(model,"pcspeaker");
        return "index";
    }

    @GetMapping("/fancontroller")
    String fancontroller(Model model) {
        makeAttr(model,"fancontroller");
        return "index";
    }

    @GetMapping("/casefan")
    String casefan(Model model) {
        makeAttr(model, "casefan");
        return "index";
    }


    private void makeAttr(Model model, String deviceName) {
        makeAttr(model, deviceName, deviceName, null);
    }

    private void makeAttr(Model model, String deviceTypeName, String deviceName1, String deviceName2) {
        Integer sortFlagModel = (Integer) model.getAttribute("sortFlag");
        int s = sortFlagModel == null ? 0 : sortFlagModel;
        List<DeviceInfo> deviceInfoList = dao.findAll(deviceName1, s);
        deviceInfoList = noPriceAfter(deviceInfoList);
        List<DeviceInfoFormatted> formattedList = makeFormattedList(deviceInfoList);

        if (deviceName2 != null) {
            deviceInfoList = dao.findAll(deviceName2, s);
            deviceInfoList = noPriceAfter(deviceInfoList);
            formattedList.addAll(makeFormattedList(deviceInfoList));
        }

        String firebaseUid = (String) model.getAttribute("firebaseUid");
        if (firebaseUid != null) {
            try {
                List<UserAssem> userAssems = firestoreService.getAssemblies(firebaseUid);
                Set<String> registeredIds = userAssems.stream()
                        .map(UserAssem::deviceid)
                        .collect(java.util.stream.Collectors.toSet());
                for (int i = 0; i < formattedList.size(); i++) {
                    if (registeredIds.contains(formattedList.get(i).id())) {
                        DeviceInfoFormatted dif = formattedList.get(i);
                        formattedList.set(i, new DeviceInfoFormatted(
                                dif.id(), dif.device(), dif.url(), dif.name(), dif.imgurl(), dif.detail(), dif.price(),
                                dif.rank(), true, "middle", 1, false, dif.flag1(), dif.flag2()
                        ));
                    }
                }
            } catch (Exception e) {
                logger.error("[HomeController] Failed to get assemblies from Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        model.addAttribute("saveHeadVisible", "hidden");
        model.addAttribute("assembliesDisplay", "hidden");
        model.addAttribute("restoredListDisplay", "hidden");
        model.addAttribute("deviceInfoList", formattedList);
        model.addAttribute("deviceTypeName", deviceTypeName);
        model.addAttribute("sortFlag", s);
        model.addAttribute("updateTime", dao.getTime().format(formatter));

    }

    private List<DeviceInfoFormatted> makeFormattedList(List<DeviceInfo> deviceInfoList) {
        List<DeviceInfoFormatted> formattedList = new ArrayList<>();
        for (DeviceInfo di : deviceInfoList) {
            formattedList.add(new DeviceInfoFormatted(
                    di.id(), deviceTypeJp.get(di.device()), di.url(), di.name(), di.imgurl(), di.detail(),
                    di.price() == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(di.price()),
                    di.rank().toString(), false, "middle", 1, false, di.flag1(), di.flag2()
            ));
        }
        return formattedList;
    }

    private List<DeviceInfoFormatted> makeFormattedList(List<DeviceInfo> deviceInfoList, Map<String, Integer> countMap) {
        List<DeviceInfoFormatted> formattedList = new ArrayList<>();
        int rowSpan;
        int deviceCount = 0;
        String tableStyle;
        boolean checked;
        for (DeviceInfo di : deviceInfoList) {
            if (deviceCount == 0) {
                tableStyle = "top";
                rowSpan = countMap.get(di.device());
                checked = true;
                if (deviceCount == countMap.get(di.device())-1) {
                    tableStyle = ""; // table style none
                }
            } else if (deviceCount == countMap.get(di.device())-1) {
                tableStyle = "bottom";
                rowSpan = 1;
                checked = false;
            } else {
                tableStyle = "middle";
                rowSpan = 1;
                checked = false;
            }

            formattedList.add(new DeviceInfoFormatted(
                    di.id(), deviceTypeJp.get(di.device()), di.url(), di.name(), di.imgurl(), di.detail(),
                    di.price() == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(di.price()),
                    di.rank().toString(), false, tableStyle, rowSpan, checked, di.flag1(), di.flag2()
            ));
            if (deviceCount == countMap.get(di.device())-1) {
                deviceCount = 0;
            } else {
                deviceCount++;
            }
        }
        return formattedList;
    }

    private List<DeviceInfo> sortList(List<DeviceInfo> deviceInfoList) {
        List<DeviceInfo> sortedList = new ArrayList<>();
        for (String dev : deviceTypeList) {
            for (DeviceInfo di : deviceInfoList) {
                if (dev.equals(di.device())) {
                    sortedList.add(di);
                }
            }
        }
        return sortedList;
    }

    private List<DeviceInfo> noPriceAfter(List<DeviceInfo> list) {
        List<DeviceInfo> retList = new ArrayList<>(list);
        for (DeviceInfo l : list) {
            if (l.price() == 0) {
                retList.remove(0);
                retList.add(l);
            } else {
                break;
            }
        }
        return retList;
    }

    @PostMapping("/add") // Add device to assemblies
    String addUserAssem(RedirectAttributes redirectAttributes, @RequestParam("id") String id, @RequestParam("devType") String deviceTypeName,
                        @RequestParam("body_scroll_px") String bodyScrollPx,
                        @RequestParam("sortFlag") String sortFlag, HttpSession session) {

        String firebaseUid = (String) session.getAttribute("firebaseUid");
        if (firebaseUid == null) {
            return String.format("redirect:/%s", deviceTypeName);
        }

        DeviceInfo di = dao.findRecordById(id);
        if (di == null) {
            logger.error("[HomeController] Device not found for id: {}", id);
            return String.format("redirect:/%s", deviceTypeName);
        }
        UserAssem assem = new UserAssem(UUID.randomUUID().toString().replace("-", ""), di.id(), di.device(), firebaseUid,
                LocalDateTime.now(), LocalDateTime.now());
        try {
            firestoreService.addAssembly(firebaseUid, assem);
        } catch (Exception e) {
            logger.error("[HomeController] Failed to add assembly to Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }

        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        redirectAttributes.addFlashAttribute("sortFlag", Integer.valueOf(sortFlag));
        return String.format("redirect:/%s", deviceTypeName);
    }

    @PostMapping("/del") // Delete device from assemblies
    String delUserAssem(RedirectAttributes redirectAttributes, @RequestParam("id") String id, @RequestParam("devType") String deviceTypeName,
                        @RequestParam("body_scroll_px") String bodyScrollPx,
                        HttpSession session) {
        String firebaseUid = (String) session.getAttribute("firebaseUid");
        if (firebaseUid == null) {
            return "redirect:/";
        }

        try {
            firestoreService.deleteAssembly(firebaseUid, id);
        } catch (Exception e) {
            logger.error("[HomeController] Failed to delete assembly from Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }

        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        return "redirect:/";
    }

    private final Map<String, Integer> sortMap = Map.of(
            "popular", 0,
            "lower", 1,
            "higher", 2,
            "newer", 3
    );
    @GetMapping("/sort") // Sort devices
    String sortDevices(RedirectAttributes redirectAttributes, @RequestParam("sort") String sort, @RequestParam("devType") String deviceTypeName,
                       @RequestParam("body_scroll_px") String bodyScrollPx) {

        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        redirectAttributes.addFlashAttribute("sortFlag", sortMap.get(sort));
        return String.format("redirect:/%s", deviceTypeName);
    }

    record SaveRec(List<String> deviceIdList) {}

    @PostMapping("/save") // Save assemblies of user's construction.
    String saveConstruction(SaveRec saveRec, HttpSession session) {
        String firebaseUid = (String) session.getAttribute("firebaseUid");
        if (saveRec.deviceIdList() == null || saveRec.deviceIdList().isEmpty() || firebaseUid == null) {
            return "redirect:/";
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        dao.save(uuid, firebaseUid, saveRec.deviceIdList());

        try {
            List<DeviceInfoDao.SaveItem> items = dao.getSaveItemsBySaveId(uuid);
            SaveHead saveHead = new SaveHead(uuid, firebaseUid, "NONAME", LocalDateTime.now(), LocalDateTime.now());
            firestoreService.saveSaves(firebaseUid, null, List.of(saveHead), Map.of(uuid, items));
        } catch (Exception e) {
            logger.error("[HomeController] Failed to save to Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }

        return "redirect:/rec/" + uuid;
    }

    public record RestoreDevice (String saveid, String deviceid, String device, String url, String name,
                          String imgurl, String detail, Integer oldprice, Integer newprice) {}
    record RestoreDeviceFormatted (String saveid, String deviceid, String device, String url, String name,
                          String imgurl, String detail, String oldprice, String newprice, String diffprice, String color) {
        static RestoreDeviceFormatted create(RestoreDevice rd) {
            int op = rd.oldprice();
            int np = rd.newprice();
            return new RestoreDeviceFormatted(rd.saveid(), rd.deviceid(), rd.device(), rd.url(), rd.name(),
                    rd.imgurl(), rd.detail(),
                    op == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(op),
                    np == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(np),
                    (np == 0 || op == 0) ? "" : new DecimalFormat("(+###,###);(-###,###)").format(np-op)
                            .replace("(+0)", "(±0)"),
                    np == op ? "black" : np > op ? "red" : "blue"
            );
        }
    }

    @GetMapping("/rec/{saveId:[0-9a-fA-F]{32}}")
    String restoreConstruction(Model model, @PathVariable String saveId) {
        saveId = saveId.toLowerCase();

        List<RestoreDevice> rdList = null;
        boolean firestoreError = false;

        // Firestore から取得を試みる（empty = ドキュメントなし、present = ドキュメントあり）
        try {
            Optional<List<DeviceInfoDao.SaveItem>> saveItems = firestoreService.getSaveItems(saveId);
            if (saveItems.isPresent()) {
                // ドキュメントと items フィールドが存在する場合はその結果を使う（空リストでも H2 にフォールバックしない）
                rdList = buildRestoreDevicesFromSaveItems(saveId, saveItems.get());
            }
        } catch (Exception e) {
            logger.error("[HomeController] Failed to get save from Firestore: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            firestoreError = true;
        }

        // Firestore にドキュメントが存在しない、または items フィールドが存在しない場合のみ H2 から取得
        if (rdList == null) {
            rdList = dao.restore(saveId);
        }

        if (rdList.isEmpty()) {
            // Firestore 障害でデータが取得できなかった可能性がある場合は 503
            if (firestoreError) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<RestoreDeviceFormatted> rdfList = rdList.stream().map(RestoreDeviceFormatted::create).toList();
        model.addAttribute("saveHeadVisible", "hidden");
        model.addAttribute("restoredList", rdfList);
        model.addAttribute("assembliesDisplay", "hidden");
        model.addAttribute("deviceListDisplay", "hidden");
        model.addAttribute("updateTime", dao.getTime().format(formatter));
        return "index";
    }

    private List<RestoreDevice> buildRestoreDevicesFromSaveItems(String saveId, List<DeviceInfoDao.SaveItem> saveItems) {
        List<String> deviceIds = saveItems.stream()
                .map(DeviceInfoDao.SaveItem::deviceId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Map<String, DeviceInfo> deviceMap = dao.findRecordByIds(deviceIds).stream()
                .collect(Collectors.toMap(DeviceInfo::id, d -> d));
        return saveItems.stream()
                .filter(item -> deviceMap.containsKey(item.deviceId()))
                .map(item -> {
                    DeviceInfo di = deviceMap.get(item.deviceId());
                    return new RestoreDevice(saveId, di.id(), di.device(), di.url(), di.name(),
                            di.imgurl(), di.detail(), item.price(), di.price());
                }).toList();
    }
}
