package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.model.DeviceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jp.developer.bbee.pcassem.constants.ApiEndPoint;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/")
public class ApiResponseController {

    private final DeviceInfoDao dao;

    @Autowired
    ApiResponseController(DeviceInfoDao dao) {
        this.dao = dao;
    }

    @GetMapping(ApiEndPoint.GET_DEVICE)
    public Map<String, List<DeviceInfo>> getDeviceList(@RequestParam(value="device", defaultValue="pccase") String device) {
        Map<String, List<DeviceInfo>> results = new HashMap<>();
        results.put("results", dao.findAll(device));
        return results;
    }

    @GetMapping(ApiEndPoint.GET_UPDATE)
    public Map<String, Integer> getLastUpdate() {
        LocalDateTime result = dao.getTime();
        return getUpdateMap(result);
    }

    @NonNull
    private Map<String, Integer> getUpdateMap(@NonNull LocalDateTime ldt) {
        var m = new HashMap<String, Integer>();
        m.put("kakakuupdate", ldt.getYear()*10000 + ldt.getMonthValue()*100 + ldt.getDayOfMonth());
        return m;
    }
}
