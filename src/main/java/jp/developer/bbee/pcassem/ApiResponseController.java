package jp.developer.bbee.pcassem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("api/")
public class ApiResponseController {

    private final DeviceInfoDao dao;

    @Autowired
    ApiResponseController(DeviceInfoDao dao) {
        this.dao = dao;
    }

    private final AtomicLong counter = new AtomicLong();
    private static final String template = "Hello, %s!";
    record JsonTest(long id, String content) {}

    @GetMapping("/test")
    List<Map<String, String>> getJsonTest(@RequestParam(value="name", defaultValue="World") String name) {
        List<Map<String, String>> jsonList = new ArrayList<>();
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("device", "pccase");
        jsonObj.put("name", "MACUBE 110");
        jsonObj.put("imgurl", "https://img1.kakaku.k-img.com/images/productimage/l/J0000034999.jpg");
        jsonObj.put("detail", "DEEPCOOL\n電源規格：ATX PS2\nMicroATX/Mini-ITX\n225x431x400 mm");
        jsonObj.put("price", "¥ 4,618");
        jsonList.add(jsonObj);

        jsonObj = new HashMap<>();
        jsonObj.put("device", "motherboard");
        jsonObj.put("name", "ROG STRIX B660-I GAMING WIFI");
        jsonObj.put("imgurl", "https://img1.kakaku.k-img.com/images/productimage/l/K0001414046.jpg");
        jsonObj.put("detail", "ASUS\nLGA1700\nMini ITX\nDIMM DDR5");
        jsonObj.put("price", "¥ 26,398");
        jsonList.add(jsonObj);

        return jsonList;
    }

    @GetMapping("/devicelist")
    public List<HomeController.DeviceInfo> getDeviceList(@RequestParam(value="device", defaultValue="pccase") String device) {
        return dao.findAll(device);
    }
}
