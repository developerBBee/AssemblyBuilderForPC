package jp.developer.bbee.pcassem;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

    record CpuItem (String id, String name, String imgUrl, String type, String value) {}

    @RequestMapping("/")
    String top(Model model) {

        String id = "00001";
        String name = "Ryzen 7 5800X3D BOX";
        String imgUrl = "https://img1.kakaku.k-img.com/images/productimage/l/K0001437357.jpg";
        String type = "AM4";
        String value = "72,545円";

        String id2 = "00002";
        String name2 = "Core i7 12700 BOX";
        String imgUrl2 = "https://www.kojima.net/ito/img_public/prod/073585/073585850/0735858503129/IMG_PATH_M/pc/0735858503129_A01.jpg";
        String type2 = "LGA1700";
        String value2 = "44,480円";

        List<CpuItem> cpuList = List.of(
                new CpuItem(id, name, imgUrl, type, value),
                new CpuItem(id2, name2, imgUrl2, type2, value2)
        );
        model.addAttribute("cpuList", cpuList);
        return "index";
    }
}
