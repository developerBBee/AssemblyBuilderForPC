package jp.developer.bbee.pcassem;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseControllerTest {

    private final ApiResponseController controller;
    private final Method method;


    ApiResponseControllerTest() throws NoSuchMethodException {
        this.controller = new ApiResponseController(null);

        // private methodを取得
        this.method = ApiResponseController.class.getDeclaredMethod("getUpdateMap", LocalDateTime.class);
        method.setAccessible(true);
    }

    @Test
    void getUpdateMapTest() throws InvocationTargetException, IllegalAccessException {

        var now = LocalDateTime.now();

        for (int i = 0; i<1000; i++) {
            var ldt = now.plusDays(i);

            // private methodを呼び出す
            var m = (Map<String, Integer>) method.invoke(controller, ldt);
            var ymd = m.get("kakakuupdate");

            // 現在日時は　作成時点(20230505)より後
            assertTrue(ymd >= 20230505);

            var year = ymd / 10000; // 年 = now/10000
            var month = ymd / 100 % 100; // 月 = now/100 してさらに /100の余り
            var day = ymd % 100; // 日 = now/100の余り
            System.out.println(i + " " + ldt + " " + ymd + " " + year + "/" + month + "/" + day);


            assertTrue(year >= 2023);
            assertTrue(month >= 1 && month <= 12);

            if (month == 2) {
                assertTrue(day >= 1 && day <= 29);
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                assertTrue(day >= 1 && day <= 30);
            } else {
                assertTrue(day >= 1 && day <= 31);
            }
        }

    }
}