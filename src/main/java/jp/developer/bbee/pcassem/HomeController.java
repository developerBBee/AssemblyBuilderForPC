package jp.developer.bbee.pcassem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class HomeController {
//    public static final String DOMAIN_NAME = "https://www.pcbuilding.link/"; // server env.
    public static final String DOMAIN_NAME = "https://localhost/"; // local env.
    public static final boolean DEBUG = false;
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm");
    private static final int MAX_RETRY = 3;
    private final DeviceInfoDao dao;
    private final KakakuClient kakakuClient;

    private LocalDateTime fullUpdateDate = LocalDateTime.of(2000, 1, 1, 0, 0);
    private LocalDateTime lastUpdateDate = LocalDateTime.of(2020, 1, 1, 0, 0);

    public Map<String, String> deviceTypeJp = new HashMap<>();

    @Autowired // <- DAO auto setting
    HomeController(DeviceInfoDao dao){
        this.dao = dao;
        kakakuClient = new KakakuClient(dao);
        updateKakaku();
        makeDeviceTypeJp();
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
        new Thread(() -> {
            runTask(); // Run task at startup
        }).start();

    }

    public void runTask() {
        boolean incomplete = true;
        boolean fullUpdate = (Duration.between(fullUpdateDate, LocalDateTime.now()).toHours() > 165); // 24*7=168
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
                lastUpdateDate = LocalDateTime.now();
                if (fullUpdate) fullUpdateDate = lastUpdateDate;
            } catch (IOException e) {
                System.out.println("update kakaku failed. reason=" + e.getMessage());
            }
        }

        Timer timer = new Timer();
        TimerTask task = new MyTimerTask(HomeController.this);
        LocalDateTime nextDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4,0,0));
        long delay = Duration.between(LocalDateTime.now(), nextDateTime).toMillis();
        //delay = 30000; // debug
        timer.schedule(task, delay); // Run task on schedule
        System.out.println("Update scheduling, delay=" + delay + "ms");
    }

    static class MyTimerTask extends TimerTask {
        private HomeController controller;
        MyTimerTask(HomeController hc) {
            this.controller = hc;
        }
        @Override
        public void run() {
            controller.runTask();
        }
    }

    record DeviceInfoFormatted (String id, String device, String url, String name, String imgurl, String detail, String price, String rank) {}
    record DeviceInfo (String id, String device, String url, String name, String imgurl, String detail, Integer price, Integer rank) {}
    record UserAssem (String id, String deviceid, String device, String guestid) {}

    @GetMapping("/")
    String top(Model model, @RequestParam(value = "guestId", required = false) String guestId) {
        model.addAttribute("deviceListDisplay", "hidden");
        model.addAttribute("updateTime", lastUpdateDate.format(formatter));

        if (guestId != null && guestId.length() >= 32) {
            List<UserAssem> userAssems = dao.findAllUserAssemByGuestId(guestId);
            List<DeviceInfo> assembliesList = new ArrayList<>();
            for (UserAssem userAssem : userAssems) {
                assembliesList.add(dao.findRecordById(userAssem.deviceid()));
            }
            List<DeviceInfoFormatted> formattedAssembliesList = makeFormattedList(assembliesList);
            if (assembliesList.size() == 0) {
                model.addAttribute("assembliesDisplay", "hidden");
            } else {
                model.addAttribute("assembliesList", formattedAssembliesList);
                int totalPrice = 0;
                for (DeviceInfo assembly : assembliesList) {
                    totalPrice += assembly.price();
                }
                /* TODO show warning if including price=0 item */
                model.addAttribute("totalPrice", new DecimalFormat("¥ ###,###").format(totalPrice));
                return "index";//"redirect:/home";
            }
        } else {
            model.addAttribute("assembliesDisplay", "hidden");
        }
        return "index";
       // return "redirect:/home";
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
        makeAttr(model, deviceTypeName, deviceName1, deviceName2, 0);
    }

    private void makeAttr(Model model, String deviceName, int sortFlag) {
        makeAttr(model, deviceName, deviceName, null,  sortFlag);
    }

    private void makeAttr(Model model, String deviceTypeName, String deviceName1, String deviceName2, int sortFlag) {
        Integer sortFlagModel = (Integer) model.getAttribute("sortFlag");
        int s = sortFlagModel == null ? sortFlag : sortFlagModel;
        List<DeviceInfo> deviceInfoList = dao.findAll(deviceName1, s);
        deviceInfoList = noPriceAfter(deviceInfoList);
        List<DeviceInfoFormatted> formattedList = makeFormattedList(deviceInfoList);

        if (deviceName2 != null) {
            deviceInfoList = dao.findAll(deviceName2, s);
            deviceInfoList = noPriceAfter(deviceInfoList);
            formattedList.addAll(makeFormattedList(deviceInfoList));
        }

        model.addAttribute("assembliesDisplay", "hidden");
        model.addAttribute("deviceInfoList", formattedList);
        model.addAttribute("deviceTypeName", deviceTypeName);
        model.addAttribute("updateTime", lastUpdateDate.format(formatter));

    }

    //record DeviceInfoFormatted (String id, String device, String url, String name, String imgurl, String detail, String price, String rank) {}
    private List<DeviceInfoFormatted> makeFormattedList(List<DeviceInfo> deviceInfoList) {
        List<DeviceInfoFormatted> formattedList = new ArrayList<>();
        for (DeviceInfo di : deviceInfoList) {
            formattedList.add(new DeviceInfoFormatted(
                    di.id(), deviceTypeJp.get(di.device()), di.url(), di.name(), di.imgurl(), di.detail(),
                    di.price == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(di.price),
                    di.rank().toString()
            ));
        }
        return formattedList;
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

    @GetMapping("/add") // Add device to assemblies
    String addUserAssem(RedirectAttributes redirectAttributes, @RequestParam("id") String id, @RequestParam("devType") String deviceTypeName,
                   @RequestParam("dev") String device, @RequestParam("guestId") String guestId, @RequestParam("body_scroll_px") String bodyScrollPx) {

        if (guestId.length() != 32) { // Issue guestId
            return String.format("redirect:%s", DOMAIN_NAME + deviceTypeName);
        }

        if (dao.findUserAssem(id, guestId) == null) {
            DeviceInfo di = dao.findRecordById(id);
            UserAssem assem = new UserAssem(UUID.randomUUID().toString().replace("-", ""), di.id(), di.device(), guestId);
            dao.addUserAssem(assem);
        } else {
            System.out.println("This is already registered. deviceid=" + id + " guestid=" + guestId);
        }


        redirectAttributes.addFlashAttribute("guestId", guestId);
        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        //return devType;
        return String.format("redirect:%s", DOMAIN_NAME + deviceTypeName);
    }

    @GetMapping("/del") // Add device to assemblies
    String delUserAssem(RedirectAttributes redirectAttributes, @RequestParam("id") String id, @RequestParam("devType") String deviceTypeName,
                        @RequestParam("dev") String device, @RequestParam("guestId") String guestId, @RequestParam("body_scroll_px") String bodyScrollPx) {
        if (guestId.length() != 32) { // Issue guestId
            return String.format("redirect:%s", DOMAIN_NAME);
        }

        dao.deleteUserAssem(id, guestId);
        redirectAttributes.addFlashAttribute("guestId", guestId);
        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        return String.format("redirect:%s", DOMAIN_NAME);
    }

    private Map<String, Integer> sortMap = Map.of(
            "popular", 0,
            "lower", 1,
            "higher", 2
    );
    @GetMapping("/sort") // Sort devices
    String sortDevices(RedirectAttributes redirectAttributes, @RequestParam("sort") String sort, @RequestParam("devType") String deviceTypeName,
                       @RequestParam("guestId") String guestId, @RequestParam("body_scroll_px") String bodyScrollPx) {

        redirectAttributes.addFlashAttribute("guestId", guestId);
        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        redirectAttributes.addFlashAttribute("sortFlag", sortMap.get(sort));
        //return devType;
        return String.format("redirect:%s", DOMAIN_NAME + deviceTypeName);
    }
}
