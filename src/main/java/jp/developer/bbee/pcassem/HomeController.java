package jp.developer.bbee.pcassem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    private final DeviceInfoDao dao;
    private final KakakuClient kakakuClient;

    @Autowired // <- DAO auto setting
    HomeController(DeviceInfoDao dao){
        this.dao = dao;
        kakakuClient = new KakakuClient(dao);

        try {
            kakakuClient.updateKakaku(true);
            /* TODO schedule updateKakaku() */
        } catch (IOException e) {
            throw new RuntimeException(e);
            /* TODO retry updateKakaku() */
        }
    }
    record Item (String id, String name, String imgurl, String type, String price, String rank) {}
    record DeviceInfo (String id, String url, String name, String imgurl, String type, Integer price, Integer rank) {}

    @RequestMapping("/")
    String top(Model model) {

        String id = "00001";
        String name = "Ryzen 7 5800X3D BOX";
        String imgUrl = "https://img1.kakaku.k-img.com/images/productimage/l/K0001437357.jpg";
        String type = "AM4";
        String price = "¥72,545";
        String rank = "1";

        String id2 = "00002";
        String name2 = "Core i7 12700 BOX";
        String imgUrl2 = "https://www.kojima.net/ito/img_public/prod/073585/073585850/0735858503129/IMG_PATH_M/pc/0735858503129_A01.jpg";
        String type2 = "LGA1700";
        String price2 = "¥44,480";
        String rank2 = "12";

        List<Item> itemList = List.of(
                new Item(id, name, imgUrl, type, price, rank),
                new Item(id2, name2, imgUrl2, type2, price2, rank2)
        );
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/pccase")
    String pccase(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("pccase");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/motherboard")
    String motherboard(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("motherboard");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/powersupply")
    String powersupply(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("powersupply");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/cpu")
    String cpu(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("cpu");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "block");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/cpucooler")
    String cpucooler(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("cpucooler");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/pcmemory")
    String pcmemory(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("pcmemory");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/storage")
    String storage(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("hdd35inch");
        List<Item> itemList_hdd = makeItemList(deviceInfoList);

        deviceInfoList = dao.findAll("ssd");
        List<Item> itemList = makeItemList(deviceInfoList);

        itemList.addAll(itemList_hdd);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/videocard")
    String videocard(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("videocard");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/ossoft")
    String ossoft(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("ossoft");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/lcdmonitor")
    String lcdmonitor(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("lcdmonitor");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/keyboard")
    String keyboard(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("keyboard");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/mouse")
    String mouse(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("mouse");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/mediadrive")
    String mediadrive(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("dvddrive");
        List<Item> itemList_dvd = makeItemList(deviceInfoList);

        deviceInfoList = dao.findAll("bluraydrive");
        List<Item> itemList = makeItemList(deviceInfoList);

        itemList.addAll(itemList_dvd);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/soundcard")
    String soundcard(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("soundcard");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/pcspeaker")
    String pcspeaker(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("pcspeaker");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/fancontroller")
    String fancontroller(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("fancontroller");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    @GetMapping("/casefan")
    String casefan(Model model) {
        List<DeviceInfo> deviceInfoList = dao.findAll("casefan");
        List<Item> itemList = makeItemList(deviceInfoList);

        //model.addAttribute("socketColumn", "hidden");
        model.addAttribute("deviceInfoList", itemList);
        return "index";
    }

    private List<Item> makeItemList(List<DeviceInfo> deviceInfoList) {
        List<Item> itemList = new ArrayList<>();
        for (DeviceInfo di : deviceInfoList) {
            itemList.add(new Item(
                    di.id(), di.name(), di.imgurl(), di.type(),
                    new DecimalFormat("¥ ###,###").format(di.price), di.rank().toString()
            ));
        }
        return itemList;
    }
}
