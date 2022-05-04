package jp.developer.bbee.pcassem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm");
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
            }
        };
        new Thread(() -> {
            task.run(); // Run task at startup
        }).start();

    }

    record Item (String id, String name, String imgurl, String type, String price, String rank) {}
    record DeviceInfo (String id, String url, String name, String imgurl, String type, Integer price, Integer rank) {}

    @RequestMapping("/")
    String top(Model model) {

//        String id = "00001";
//        String name = "Ryzen 7 5800X3D BOX";
//        String imgUrl = "https://img1.kakaku.k-img.com/images/productimage/l/K0001437357.jpg";
//        String type = "AM4";
//        String price = "¥72,545";
//        String rank = "1";
//
//        String id2 = "00002";
//        String name2 = "Core i7 12700 BOX";
//        String imgUrl2 = "https://www.kojima.net/ito/img_public/prod/073585/073585850/0735858503129/IMG_PATH_M/pc/0735858503129_A01.jpg";
//        String type2 = "LGA1700";
//        String price2 = "¥44,480";
//        String rank2 = "12";
//
//        List<Item> itemList = List.of(
//                new Item(id, name, imgUrl, type, price, rank),
//                new Item(id2, name2, imgUrl2, type2, price2, rank2)
//        );
//        model.addAttribute("deviceInfoList", itemList);
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
        makeAttr(model, "ssd", "hdd35inch");
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
        makeAttr(model,  "bluraydrive", "dvddrive");
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
        makeAttr(model, deviceName, null);
    }

    private void makeAttr(Model model, String deviceName1, String deviceName2) {
        List<DeviceInfo> deviceInfoList = dao.findAll(deviceName1);
        List<Item> itemList = makeItemList(deviceInfoList);

        if (deviceName2 != null) {
            deviceInfoList = dao.findAll(deviceName2);
            List<Item> itemList2 = makeItemList(deviceInfoList);
            itemList.addAll(itemList2);
        }

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        model.addAttribute("updateTime", lastUpdateDate.format(formatter));
    }

    private List<Item> makeItemList(List<DeviceInfo> deviceInfoList) {
        List<Item> itemList = new ArrayList<>();
        for (DeviceInfo di : deviceInfoList) {
            itemList.add(new Item(
                    di.id(), di.name(), di.imgurl(), di.type(),
                    di.price == 0 ? "価格情報なし" : new DecimalFormat("¥ ###,###").format(di.price),
                    di.rank().toString()
            ));
        }
        return itemList;
    }
}
