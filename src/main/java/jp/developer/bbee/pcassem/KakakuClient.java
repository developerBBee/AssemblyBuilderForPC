package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.HomeController.DeviceInfo;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static jp.developer.bbee.pcassem.HomeController.formatter;

public class KakakuClient {
    public static final boolean DEBUG = true;
    public static final String DOMAIN = "kakaku.com";
    private final DeviceInfoDao dao;

    public boolean unAcquired;
    private List<String> urlList;
    private List<String> devices;
    private Map<String, String> deviceUrl;

    KakakuClient(DeviceInfoDao dao) {
        this.dao = dao;

        urlList = new ArrayList<>();
        urlList.add("/pc/pc-case/ranking_0580/"); // PC case
        urlList.add("/pc/motherboard/ranking_0540/"); // Motherboard
        urlList.add("/pc/power-supply/ranking_0590/"); // Power supply unit
        urlList.add("/pc/cpu/ranking_0510/"); // CPU
        urlList.add("/pc/cpu-cooler/ranking_0512/"); // CPU cooler
        urlList.add("/pc/pc-memory/ranking_0520/"); // Memory
        urlList.add("/pc/hdd-35inch/ranking_0530/"); // Storage HDD
        urlList.add("/pc/ssd/ranking_0537/"); // Storage SSD
        urlList.add("/pc/videocard/ranking_0550/"); // Graphic board
        urlList.add("/pc/os-soft/ranking_0310/"); // OS soft
        urlList.add("/pc/lcd-monitor/ranking_0085/"); // Display
        urlList.add("/pc/keyboard/ranking_0150/"); // Keyboard
        urlList.add("/pc/mouse/ranking_0160/"); // Mouse
        urlList.add("/pc/dvd-drive/ranking_0125/"); // DVD media drive
        urlList.add("/pc/blu-ray-drive/ranking_0126/"); // Blue-rya media drive
        urlList.add("/pc/sound-card/ranking_0560/"); // Sound card
        urlList.add("/pc/pc-speaker/ranking_0170/"); // Speaker
        urlList.add("/pc/fan-controller/ranking_0582/"); // Fan controller
        urlList.add("/pc/case-fan/ranking_0581/"); // Case fan

        devices = new ArrayList<>();
        deviceUrl = new HashMap<>();
        for (String url : urlList) {
            String device = url.replace("/pc/","");
            device = device.replaceAll("/.*/", "");
            device = device.replaceAll("-", "");
            devices.add(device);
            deviceUrl.put(device, url);
        }
    }

    public void getKakaku() throws IOException {
        SocketFactory factory = SSLSocketFactory.getDefault();
        for (String device : devices) {
            try (var soc = factory.createSocket(DOMAIN, 443);
                 var pw = new PrintWriter(soc.getOutputStream());
                 var isr = new InputStreamReader(soc.getInputStream());
                 var bur = new BufferedReader(isr)
            ) {
                pw.println("GET " + deviceUrl.get(device) + " HTTP/1.1");
                pw.println("Host: " + DOMAIN);
                pw.println();
                pw.flush();
                try {
                    List<String> linkColumns = bur.lines()
                            .limit(DEBUG ? 500 : 10000)
                            .filter(s -> s.contains("rkgBoxLink"))
                            .map(s -> s.replaceAll("<a href=\"", ""))
                            .map(s -> s.replaceAll("\" class=.*", ""))
                            .toList();

                    for (String linkUrl : linkColumns) {
                        DeviceInfo di = dao.findRecordByUrl(linkUrl, device);
                        if (di == null) {
                            dao.add(new DeviceInfo(
                                    UUID.randomUUID().toString().replace("-", ""),
                                    device,
                                    linkUrl,
                                    "",
                                    "",
                                    "",
                                    0,
                                    99
                            ));
                        }
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("URLを取得できませんでした reason=" + e.getMessage());
                }
            } catch (SocketException e) {
                System.out.println("getKakaku() SocketException reason=" + e.getMessage());
            }
            System.out.println("getKakaku() device=" + device + " time=" + LocalDateTime.now().format(formatter));
        }

        updateKakaku(false); // full update with argument false
    }

    String newName;
    String newImgUrl;
    String newDetail;
    Integer newPrice;
    Integer newRank;
    public void updateKakaku(boolean fastUpdate) throws IOException {
        unAcquired = false; // Acquired link url

        for (String device : devices) {
            List<DeviceInfo> deviceInfoList = dao.findAll(device);

            SocketFactory factory = SSLSocketFactory.getDefault();
            for (DeviceInfo deviceInfo : deviceInfoList) {
                if ((fastUpdate || DEBUG) &&
                        !( "".equals(deviceInfo.name())
                        || "".equals(deviceInfo.imgurl())
                        || 0  == deviceInfo.price()
                        || 99 == deviceInfo.rank()
                )) continue; // Not get data if id is not empty in fastUpdate.

                try (var soc = factory.createSocket(DOMAIN, 443);
                     var pw = new PrintWriter(soc.getOutputStream());
                     var isr = new InputStreamReader(soc.getInputStream(), "SJIS"); // kakaku SHIFT_JIS
                     var bur = new BufferedReader(isr)
                ) {
                    pw.println("GET " + deviceInfo.url() + " HTTP/1.1");
                    pw.println("Host: " + DOMAIN);
                    pw.println();
                    pw.flush();

                    newName = "";
                    newImgUrl = "";
                    newDetail = "";
                    newPrice = 0;
                    newRank = 99;
                    try {
                        bur.lines().limit(1000).forEach(str -> {

                            String s = "";
                            try {
                                s = StringEncoder.sjisToUtf8(str);
                            } catch (UnsupportedEncodingException e) {
                                System.out.println("charset failed, reason=" + e.getMessage());
                            }
                            if (s.contains("  prdname: ")) {
                                newName = s.substring(12, s.length()-2);
                                newName = newName.replace("\\", ""); // delete yen mark
                            } else if (s.contains("  prdlprc: ")) {
                                try {
                                    newPrice = Integer.valueOf(s.substring(11, s.length()-1));
                                } catch (NumberFormatException e) {
                                    System.out.println("価格が数値外：" + e.getMessage());
                                }
                            } else if (s.contains("売れ筋ランキング：")) {
                                try {
                                    newRank = Integer.valueOf(s.substring(
                                            s.indexOf("売れ筋ランキング：") + "売れ筋ランキング：".length(), s.indexOf("位")));
                                } catch (NumberFormatException e) {
                                    System.out.println("ランキングが数値外：" + e.getMessage());
                                }
                            } else if (s.contains("width=\"160\" height=\"120\" border=\"0\"")) {
                                newImgUrl = s.substring(s.indexOf(" src=\"https:") + 6,
                                        s.indexOf("\" width=\"160\" height=\"120\" border=\"0\""));
                            } else if (s.contains("ソケット形状：") && ("cpu".equals(device) || "motherboard".equals(device))) {
                                newDetail = s.substring(s.indexOf("ソケット形状：") + "ソケット形状：".length(),
                                        s.contains("二次キャッシュ") ? s.indexOf("二次キャッシュ") - 1
                                                : s.indexOf("ソケット形状：") + "ソケット形状：".length() + 10);
                                newDetail = newDetail.replace("<sp", "").replace("Socket ", "");
                            }
                        });
                        dao.update(new DeviceInfo(
                                deviceInfo.id(), deviceInfo.device(), deviceInfo.url(), newName, newImgUrl, newDetail, newPrice, newRank));

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("価格情報を取得できませんでした reason=" + e.getMessage());
                    }
                } catch (SocketException e) {
                    System.out.println("updateKakaku() SocketException reason=" + e.getMessage());
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("updateKakaku() device=" + device + " time=" + LocalDateTime.now().format(formatter));
        }
    }
}
