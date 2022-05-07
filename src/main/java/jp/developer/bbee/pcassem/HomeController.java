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
    public static final boolean DEBUG = false;
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm");
    private static final int MAX_RETRY = 3;
    private final DeviceInfoDao dao;
    private final KakakuClient kakakuClient;

    private LocalDateTime fullUpdateDate = LocalDateTime.of(2000, 1, 1, 0, 0);
    private LocalDateTime lastUpdateDate = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Autowired // <- DAO auto setting
    HomeController(DeviceInfoDao dao){
        this.dao = dao;
        kakakuClient = new KakakuClient(dao);
        updateKakaku();
    }

    private void updateKakaku() {

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
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
                LocalDateTime nextDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4,0,0));
                long delay = Duration.between(LocalDateTime.now(), nextDateTime).toMillis();
                timer.schedule(this, delay); // Run task on schedule
                System.out.println("Update scheduling, delay=" + delay + "ms");
            }
        };
        if (DEBUG) return;
        new Thread(() -> {
            task.run(); // Run task at startup
        }).start();

    }

    record DeviceInfoFormatted (String id, String device, String url, String name, String imgurl, String detail, String price, String rank) {}
    record DeviceInfo (String id, String device, String url, String name, String imgurl, String detail, Integer price, Integer rank) {}
    record UserAssem (String id, String deviceid, String device, String guestid) {}

    @GetMapping("/")
    String top(Model model, @RequestParam(value = "guestId", required = false) String guestId) {
        model.addAttribute("deviceListDisplay", "hidden");

        if (guestId != null && guestId.length() >= 32) {
            List<UserAssem> userAssems = dao.findAllUserAssemByGuestId(guestId);
            List<DeviceInfo> assembliesList = new ArrayList<>();
            for (UserAssem userAssem : userAssems) {
                assembliesList.add(dao.findRecordById(userAssem.deviceid(), userAssem.device()));
            }
            model.addAttribute("assembliesList", assembliesList);
            return "index";//"redirect:/home";
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
        List<DeviceInfo> deviceInfoList = dao.findAll(deviceName1);
        List<DeviceInfoFormatted> formattedList = makeFormattedList(deviceInfoList, deviceName1);

        if (deviceName2 != null) {
            deviceInfoList = dao.findAll(deviceName2);
            List<DeviceInfoFormatted> formattedList2 = makeFormattedList(deviceInfoList, deviceName2);
            formattedList.addAll(formattedList2);
        }

        model.addAttribute("assembliesDisplay", "hidden");
        model.addAttribute("deviceInfoList", formattedList);
        model.addAttribute("deviceTypeName", deviceTypeName);
        model.addAttribute("updateTime", lastUpdateDate.format(formatter));

    }

    //record DeviceInfoFormatted (String id, String device, String url, String name, String imgurl, String detail, String price, String rank) {}
    private List<DeviceInfoFormatted> makeFormattedList(List<DeviceInfo> deviceInfoList, String device) {
        List<DeviceInfoFormatted> formattedList = new ArrayList<>();
        for (DeviceInfo di : deviceInfoList) {
            formattedList.add(new DeviceInfoFormatted(
                    di.id(), di.device(), di.url(), di.name(), di.imgurl(), di.detail(),
                    di.price == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(di.price),
                    di.rank().toString()
            ));
        }
        return formattedList;
    }
    @GetMapping("/add") // Add device to assemblies
    String addUserAssem(RedirectAttributes redirectAttributes, @RequestParam("id") String id, @RequestParam("devType") String deviceTypeName,
                   @RequestParam("dev") String device, @RequestParam("guestId") String guestId, @RequestParam("body_scroll_px") String bodyScrollPx) {

        if (guestId.length() != 32) { // Issue guestId
            return String.format("redirect:/%s", deviceTypeName);
        }

        if (dao.findUserAssem(id, guestId) == null) {
            UserAssem assem = new UserAssem(UUID.randomUUID().toString().replace("-", ""), id, device, guestId);
            dao.addUserAssem(assem);
        } else {
            System.out.println("This is already registered. deviceid=" + id + " guestid=" + guestId);
        }


        redirectAttributes.addFlashAttribute("guestId", guestId);
        redirectAttributes.addFlashAttribute("bodyScrollPx", bodyScrollPx);
        //return devType;
        return String.format("redirect:/%s", deviceTypeName);
    }
}
