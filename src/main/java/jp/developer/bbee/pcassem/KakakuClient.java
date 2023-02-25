package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.HomeController.DeviceInfo;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;

import static jp.developer.bbee.pcassem.HomeController.formatter;

public class KakakuClient {
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_FAST = false;
    public static final String KAKAKU_DOMAIN = "kakaku.com";
    private final DeviceInfoDao dao;

    // Flag 1 definition
    public static final int FLAG1_PSU_FLEXATX = 1 << 0;
    public static final int FLAG1_PSU_TFX = 1 << 1;
    public static final int FLAG1_PSU_SFX = 1 << 2;
    public static final int FLAG1_PSU_SFXL = 1 << 3;
    public static final int FLAG1_PSU_ATX = 1 << 4;
    public static final int FLAG1_PSU_EPS = 1 << 5;
    public static final int FLAG1_PSU_BUILTIN = 1 << 7;

    public static final int FLAG1_MOTHER_ITX = 1 << 8;
    public static final int FLAG1_MOTHER_FLEXATX = 1 << 9;
    public static final int FLAG1_MOTHER_MICROATX = 1 << 10;
    public static final int FLAG1_MOTHER_ATX = 1 << 11;
    public static final int FLAG1_MOTHER_EATX = 1 << 12;
    public static final int FLAG1_MOTHER_XLATX = 1 << 13;

    public static final int FLAG1_VOLTAGE_UNIT = 10; // /10
    public static final int FLAG1_VOLTAGE_SHIFT = 16; // *2^16

    public static final int FLAG1_SIZE_UNIT = 10; // /10
    public static final int FLAG1_SIZE_SHIFT = 24; // *2^24
    public static final int FLAG1_SIZE_RADIATOR = 1 << 31;

    // Flag 2 definition
    public static final int FLAG2_SOCKET_LGA1155 = 1 << 0;
    public static final int FLAG2_SOCKET_LGA1150 = 1 << 1;
    public static final int FLAG2_SOCKET_LGA1151 = 1 << 2;
    public static final int FLAG2_SOCKET_LGA1200 = 1 << 3;
    public static final int FLAG2_SOCKET_LGA1700 = 1 << 4;
    public static final int FLAG2_SOCKET_LGA2011 = 1 << 5;
    public static final int FLAG2_SOCKET_LGA20113 = 1 << 6;
    public static final int FLAG2_SOCKET_LGA2066 = 1 << 7;

    public static final int FLAG2_SOCKET_AM4 = 1 << 8;
    public static final int FLAG2_SOCKET_AM5 = 1 << 9;
    public static final int FLAG2_SOCKET_TR4 = 1 << 10;
    public static final int FLAG2_SOCKET_STRX4 = 1 << 11;
    public static final int FLAG2_SOCKET_SWRX8 = 1 << 12;

    public static final int FLAG2_DIMM_DDR3 = 1 << 16;
    public static final int FLAG2_DIMM_DDR4 = 1 << 17;
    public static final int FLAG2_DIMM_DDR5 = 1 << 18;
    public static final int FLAG2_SODIMM = 1 << 23; // For determining DIMM(0) or SODIMM(1)

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
            try (var soc = factory.createSocket(KAKAKU_DOMAIN, 443);
                 var pw = new PrintWriter(soc.getOutputStream());
                 var isr = new InputStreamReader(soc.getInputStream());
                 var bur = new BufferedReader(isr)
            ) {
                pw.println("GET " + deviceUrl.get(device) + " HTTP/1.1");
                pw.println("Host: " + KAKAKU_DOMAIN);
                pw.println();
                pw.flush();
                try {
                    int debugRows = 500;
                    if ("mouse".equals(device)) { // debug device
                        debugRows = 4000;
                    }
                    List<String> linkColumns = bur.lines()
                            .limit(DEBUG ? debugRows : 4000)
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
                                    99,
                                    0,
                                    0
                            ));
                        }
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("URLを取得できませんでした reason=" + e.getMessage());
                }
            } catch (SocketException e) {
                System.out.println("getKakaku() SocketException reason=" + e.getMessage());
            } catch (UncheckedIOException e) {
                System.out.println("getKakaku() failed device=" + device + " reason=" + e.getMessage());
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
    int newFlag1;
    int newFlag2;
    public void updateKakaku(boolean fastUpdate) throws IOException {
        unAcquired = false; // Acquired link url
        SocketFactory factory = SSLSocketFactory.getDefault();

        for (String device : devices) {
            if (!(fastUpdate || DEBUG_FAST)) dao.rankReset(device);
            List<DeviceInfo> deviceInfoList = dao.findAll(device);

            for (DeviceInfo deviceInfo : deviceInfoList) {
                if ((fastUpdate || DEBUG_FAST) &&
                        !( "".equals(deviceInfo.name())
                        || "".equals(deviceInfo.imgurl())
                        || "".equals(deviceInfo.detail())
                        || 0  == deviceInfo.price()
                        || 99 == deviceInfo.rank() )
                ) continue; // Not get data if id is not empty in fastUpdate.

                try (var soc = factory.createSocket(KAKAKU_DOMAIN, 443);
                     var pw = new PrintWriter(soc.getOutputStream());
                     var isr = new InputStreamReader(soc.getInputStream(), "SJIS"); // kakaku SHIFT_JIS
                     var bur = new BufferedReader(isr)
                ) {
                    pw.println("GET " + deviceInfo.url() + "spec/ HTTP/1.1");
                    pw.println("Host: " + KAKAKU_DOMAIN);
                    pw.println();
                    pw.flush();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    newName = "";
                    newImgUrl = "";
                    newDetail = "";
                    newPrice = 0;
                    newRank = 99;
                    newFlag1 = 0;
                    newFlag2 = 0;
                    try {
                        boolean isGetHtml = false;
                        if (bur.readLine().contains("200 OK")) isGetHtml = true;
                        if (isGetHtml) {
                            bur.lines().limit(1000).forEach(str -> {

                                String s = "";
                                try {
                                    s = StringEncoder.sjisToUtf8(str);
                                } catch (UnsupportedEncodingException e) {
                                    System.out.println("charset failed, reason=" + e.getMessage());
                                }
                                if (s.contains("  prdname: ")) {
                                    newName = s.substring(12, s.length() - 2);
                                    newName = newName.replace("\\", ""); // delete BS mark
                                    if ("cpu".equals(device)) setCpuPowerConsumption(newName);
                                } else if (s.contains("  prdlprc: ")) {
                                    try {
                                        newPrice = Integer.valueOf(s.substring(11, s.length() - 1));
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
                                } else if (s.contains("  mkrname: ")) {
                                    String mkrName = s.substring(12, s.length() - 2);
                                    if ("".equals(newDetail)) {
                                        newDetail = mkrName;
                                    } else {
                                        newDetail += "\n" + mkrName;
                                    }
                                } else if (s.contains("itemviewColor03b textL")) {
                                    String detailStr = getDetailComment(s, device);
                                    if (detailStr != null) {
                                        if ("".equals(newDetail)) {
                                            newDetail = detailStr;
                                        } else {
                                            newDetail += "\n" + detailStr;
                                        }
                                    }
                                }
                            });
                            dao.update(new DeviceInfo(
                                    deviceInfo.id(), deviceInfo.device(), deviceInfo.url(), newName, newImgUrl, newDetail, newPrice, newRank,
                                    newFlag1, newFlag2));
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("価格情報を取得できませんでした reason=" + e.getMessage());
                    }
                } catch (SocketException e) {
                    System.out.println("updateKakaku() SocketException reason=" + e.getMessage());
                } catch (UncheckedIOException e) {
                    System.out.println("updateKakaku() failed name=" + deviceInfo.name() + " reason=" + e.getMessage());
                }
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
            System.out.println("updateKakaku() device=" + device + " time=" + LocalDateTime.now().format(formatter));
        }
    }

    private String getDetailComment(String s, String device) {
        if (!s.contains("</td>")) return null;
        String retStr = null;
        String buf = s.replace("</td>", "");
        buf = buf.replace("</a>", "");
        buf = buf.replace("<br>", "/");
        buf = buf.replace("&#215;", " x");

        try {
            String detail = buf.substring(buf.lastIndexOf(">") + 1);
            if ("".equals(detail.replace(" ", "").replace("　", ""))) return null;

            switch (device) {
                case "pccase":
                    if (buf.contains("電源規格")) {
                        if (buf.contains("内蔵") || buf.contains("搭載")) {
                            retStr = detail;
                            newFlag1 |= FLAG1_PSU_BUILTIN;
                        } else {
                            retStr = "電源規格：" + detail;
                            if (detail.contains("EPS")) {
                                newFlag1 |= FLAG1_PSU_EPS;
                            }
                            if (detail.contains("ATX") && !detail.contains("lexATX")) {
                                newFlag1 |= FLAG1_PSU_ATX;
                            }
                            if (detail.contains("SFX-L")) {
                                newFlag1 |= FLAG1_PSU_SFXL;
                            }
                            if (detail.contains("SFX")) { // Intentional to include SFX-L standard
                                newFlag1 |= FLAG1_PSU_SFX;
                            }
                            if (detail.contains("TFX")) {
                                newFlag1 |= FLAG1_PSU_TFX;
                            }
                        }
                    } else if (buf.contains("対応マザーボード")) {
                        retStr = detail;
                        if (detail.contains("ITX")) {
                            newFlag1 |= FLAG1_MOTHER_ITX;
                        }
                        if (detail.contains("Flex") || detail.contains("flex")) {
                            newFlag1 |= FLAG1_MOTHER_FLEXATX;
                        }
                        if (detail.contains("Micro") || detail.contains("micro")) {
                            newFlag1 |= FLAG1_MOTHER_MICROATX;
                        }
                        if (detail.contains("Extended") || detail.contains("E-ATX") || detail.contains("EATX")) {
                            newFlag1 |= FLAG1_MOTHER_EATX;
                        }
                        if (detail.contains("XL-ATX") || detail.contains("XLATX")) {
                            newFlag1 |= FLAG1_MOTHER_XLATX;
                        }
                        if (detail.substring(0, 3).contains("ATX")) {
                            newFlag1 |= FLAG1_MOTHER_ATX;
                        }
                    } else if (buf.contains("幅x高さx奥行")) {
                        retStr = detail;
                        try {
                            double width = Double.parseDouble(detail.substring(0, detail.indexOf("x")).replace(" ", ""));
                            newFlag1 |= (int) Math.ceil(width / FLAG1_SIZE_UNIT) << FLAG1_SIZE_SHIFT;
                        } catch (NumberFormatException e) {
                            System.out.println("PC case \"width\" could not be obtained numerically, reason=" + e.getMessage());
                        }
                    }
                    break;
                case "motherboard":
                    if (buf.contains("CPUソケット")) {
                        retStr = detail;
                        if (detail.contains("LGA1155")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1155;
                        } else if (detail.contains("LGA1150")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1150;
                        } else if (detail.contains("LGA1151")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1151;
                        } else if (detail.contains("LGA1200")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1200;
                        } else if (detail.contains("LGA1700")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1700;
                        } else if (detail.contains("LGA2011-3")) {
                            newFlag2 |= FLAG2_SOCKET_LGA20113;
                        } else if (detail.contains("LGA2011")) {
                            newFlag2 |= FLAG2_SOCKET_LGA2011;
                        } else if (detail.contains("LGA2066")) {
                            newFlag2 |= FLAG2_SOCKET_LGA2066;
                        } else if (detail.contains("AM4")) {
                            newFlag2 |= FLAG2_SOCKET_AM4;
                        } else if (detail.contains("AM5")) {
                            newFlag2 |= FLAG2_SOCKET_AM5;
                        } else if (detail.contains("TR4")) {
                            newFlag2 |= FLAG2_SOCKET_TR4;
                        } else if (detail.contains("sTRX4")) {
                            newFlag2 |= FLAG2_SOCKET_STRX4;
                        } else if (detail.contains("sWRX8")) {
                            newFlag2 |= FLAG2_SOCKET_SWRX8;
                        }
                    } else if (buf.contains("フォームファクタ")) {
                        retStr = detail;
                        if (detail.contains("ITX")) {
                            newFlag1 |= FLAG1_MOTHER_ITX;
                        } else if (detail.contains("Flex") || detail.contains("flex")) {
                            newFlag1 |= FLAG1_MOTHER_FLEXATX;
                        } else if (detail.contains("Micro") || detail.contains("micro")) {
                            newFlag1 |= FLAG1_MOTHER_MICROATX;
                        } else if (detail.contains("Extended") || detail.contains("E-ATX") || detail.contains("EATX")) {
                            newFlag1 |= FLAG1_MOTHER_EATX;
                        } else if (detail.contains("XL-ATX") || detail.contains("XLATX")) {
                            newFlag1 |= FLAG1_MOTHER_XLATX;
                        } else if (detail.contains("ATX")) {
                            newFlag1 |= FLAG1_MOTHER_ATX;
                        }
                    } else if (buf.contains("詳細メモリタイプ")) {
                        retStr = detail;
                        if (detail.contains("S.O.DIMM")) {
                            newFlag2 |= FLAG2_SODIMM;
                        }
                        if (detail.contains("DDR3")) {
                            newFlag2 |= FLAG2_DIMM_DDR3;
                        } else if (detail.contains("DDR4")) {
                            newFlag2 |= FLAG2_DIMM_DDR4;
                        } else if (detail.contains("DDR5")) {
                            newFlag2 |= FLAG2_DIMM_DDR5;
                        }
                    }
                    break;
                case "powersupply":
                    if (buf.contains("対応規格")) {
                        retStr = detail;
                        if (detail.contains("EPS")) {
                            newFlag1 |= FLAG1_PSU_EPS;
                        }
                        if (detail.contains("ATX") && !detail.contains("lexATX")) {
                            newFlag1 |= FLAG1_PSU_ATX;
                        }
                        if (detail.contains("SFX-L")) {
                            newFlag1 |= FLAG1_PSU_SFXL;
                        }
                        if (detail.contains("SFX")) { // Intentional to include SFX-L standard
                            newFlag1 |= FLAG1_PSU_SFX;
                        }
                        if (detail.contains("TFX")) {
                            newFlag1 |= FLAG1_PSU_TFX;
                        }
                    } else if (buf.contains("電源容量")) {
                        retStr = detail;
                        newFlag1 |= Integer.parseInt(detail.substring(0, detail.indexOf("W")).replace(" ", ""))
                                / FLAG1_VOLTAGE_UNIT << FLAG1_VOLTAGE_SHIFT;
                    } else if (buf.contains("80PLUS認証")) {
                        retStr = "80PLUS " + detail;
                    }
                    break;
                case "cpu":
                    if (buf.contains("ソケット形状")) {
                        retStr = detail;
                        if (detail.contains("LGA1155")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1155;
                        } else if (detail.contains("LGA1150")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1150;
                        } else if (detail.contains("LGA1151")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1151;
                        } else if (detail.contains("LGA1200")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1200;
                        } else if (detail.contains("LGA1700")) {
                            newFlag2 |= FLAG2_SOCKET_LGA1700;
                        } else if (detail.contains("LGA2011-3")) {
                            newFlag2 |= FLAG2_SOCKET_LGA20113;
                        } else if (detail.contains("LGA2011")) {
                            newFlag2 |= FLAG2_SOCKET_LGA2011;
                        } else if (detail.contains("LGA2066")) {
                            newFlag2 |= FLAG2_SOCKET_LGA2066;
                        } else if (detail.contains("AM4")) {
                            newFlag2 |= FLAG2_SOCKET_AM4;
                        } else if (detail.contains("AM5")) {
                            newFlag2 |= FLAG2_SOCKET_AM5;
                        } else if (detail.contains("TR4")) {
                            newFlag2 |= FLAG2_SOCKET_TR4;
                        } else if (detail.contains("sTRX4")) {
                            newFlag2 |= FLAG2_SOCKET_STRX4;
                        } else if (detail.contains("sWRX8")) {
                            newFlag2 |= FLAG2_SOCKET_SWRX8;
                        }
                    } else if (buf.contains("フォームファクタ")) {
                        retStr = detail;
                    } else if (buf.contains("コア数")) {
                        retStr = detail;
                    } else if (buf.contains(">クロック周波数<")) {
                        retStr = "ベースクロック " + detail;
                    } else if (buf.contains("最大動作クロック周波数")) {
                        retStr = "最大クロック " + detail;
                    } else if (buf.contains("スレッド数")) {
                        retStr = detail + " スレッド";
                    }
                    break;
                case "cpucooler":
                    if (buf.contains("Intel対応ソケット")) {
                        retStr = detail;
                    } else if (buf.contains("AMD対応ソケット")) {
                        retStr = detail;
                    } else if (buf.contains("タイプ")) {
                        retStr = detail;
                    } else if (buf.contains("幅x高さx奥行")) {
                        retStr = detail;
                        try {
                            double height = Double.parseDouble(detail.split("x")[1].replace(" ", ""));
                            newFlag1 |= (int) Math.ceil(height / FLAG1_SIZE_UNIT) << FLAG1_SIZE_SHIFT;
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("CPU cooler \"height\" could not found, reason=" + e.getMessage());
                        } catch (NumberFormatException e) {
                            System.out.println("CPU cooler \"height\" could not be obtained numerically, reason=" + e.getMessage());
                        }
                    } else if (buf.contains("ラジエーターサイズ")) {
                        retStr = "ラジエーター：" + detail;
                        try {
                            int sizeBit = 0B11111111 << FLAG1_SIZE_SHIFT;
                            if ((newFlag1 & sizeBit) != 0) {
                                System.out.println("Size is already set");
                            }
                            double height = Double.parseDouble(detail.split("x")[1].replace(" ", ""));
                            /* TODO Change to width (index = 0) and compare with case depth */
                            newFlag1 |= (int) Math.ceil(height / FLAG1_SIZE_UNIT) << FLAG1_SIZE_SHIFT;
                            newFlag1 |= FLAG1_SIZE_RADIATOR;
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("CPU cooler \"height\" could not found, reason=" + e.getMessage());
                        } catch (NumberFormatException e) {
                            System.out.println("CPU cooler \"height\" could not be obtained numerically, reason=" + e.getMessage());
                        }
                    }
                    break;
                case "pcmemory":
                    if (buf.contains("メモリ容量")) {
                        retStr = detail;
                    } else if (buf.contains("枚数")) {
                        retStr = detail;
                    } else if (buf.contains("メモリインターフェイス")) {
                        retStr = detail;
                        if (detail.contains("S.O.DIMM")) {
                            newFlag2 |= FLAG2_SODIMM;
                        }
                    } else if (buf.contains("メモリ規格")) {
                        retStr = detail;
                        if (detail.contains("DDR3")) {
                            newFlag2 |= FLAG2_DIMM_DDR3;
                        } else if (detail.contains("DDR4")) {
                            newFlag2 |= FLAG2_DIMM_DDR4;
                        } else if (detail.contains("DDR5")) {
                            newFlag2 |= FLAG2_DIMM_DDR5;
                        }
                    } else if (buf.contains("データ転送速度")) {
                        retStr = detail;
                    }
                    break;
                case "ssd":
                case "hdd35inch":
                    if (buf.contains("容量")) {
                        retStr = detail;
                    } else if (buf.contains("規格サイズ")) {
                        retStr = detail;
                    } else if (buf.contains("インターフェイス")) {
                        retStr = detail;
                    } else if (buf.contains(">読込速度<")) {
                        retStr = "読込: " + detail;
                    } else if (buf.contains(">書込速度<")) {
                        retStr = "書込: " + detail;
                    }
                    break;
                case "videocard":
                    if (buf.contains("搭載チップ")) {
                        retStr = detail;
                    } else if (buf.contains(">メモリ<")) {
                        retStr = detail;
                    } else if (buf.contains("バスインターフェイス")) {
                        retStr = detail;
                    } else if (buf.contains("モニタ端子")) {
                        retStr = detail;
                    } else if (buf.contains("消費電力")) {
                        retStr = "消費電力:" + detail;
                        newFlag1 |= Integer.parseInt(detail.substring(0, detail.indexOf("W")).replace(" ", ""))
                                / FLAG1_VOLTAGE_UNIT << FLAG1_VOLTAGE_SHIFT;
                    } else if (buf.contains("補助電源")) {
                        retStr = "補助電源: " + detail;
                    }
                    break;
                case "lcdmonitor":
                    if (buf.contains("モニタサイズ")) {
                        retStr = detail;
                    } else if (buf.contains("アスペクト比")) {
                        retStr = detail;
                    } else if (buf.contains("表面処理")) {
                        retStr = detail;
                    } else if (buf.contains("パネル種類")) {
                        retStr = "パネル: " + detail;
                    } else if (buf.contains("解像度")) {
                        retStr = "解像度: " + detail;
                    } else if (buf.contains("応答速度")) {
                        retStr = "応答速度: " + detail;
                    } else if (buf.contains("リフレッシュレート")) {
                        retStr = "リフレッシュレート: " + detail;
                    } else if (buf.contains("入力端子")) {
                        retStr = detail;
                    }
                    break;
                case "keyboard":
                    if (buf.contains("ケーブル")) {
                        retStr = detail;
                    } else if (buf.contains("キーレイアウト")) {
                        retStr = detail;
                    } else if (buf.contains("キースイッチ")) {
                        retStr = detail;
                    }
                    break;
                case "mouse":
                    if (buf.contains("タイプ")) {
                        retStr = detail;
                    } else if (buf.contains(">ケーブル<")) {
                        retStr = detail;
                    } else if (buf.contains("ボタン数")) {
                        retStr = detail;
                    } else if (buf.contains("解像度")) {
                        retStr = detail;
                    } else if (buf.contains("重さ")) {
                        retStr = detail;
                    }
                    break;
                case "dvddrive":
                    if (buf.contains("設置方式")) {
                        retStr = detail;
                    } else if (buf.contains("接続インターフェース")) {
                        retStr = detail;
                    } else if (buf.contains("対応メディア")) {
                        retStr = detail;
                    }
                    break;
                case "bluraydrive":
                    if (buf.contains("設置方式")) {
                        retStr = detail;
                    } else if (buf.contains("接続インターフェース")) {
                        retStr = detail;
                    }
                    break;
                case "soundcard":
                    if (buf.contains("タイプ")) {
                        retStr = detail;
                    } else if (buf.contains("インターフェース")) {
                        retStr = detail;
                    } else if (buf.contains("サラウンド機能")) {
                        retStr = detail;
                    }
                    break;
                case "pcspeaker":
                    if (buf.contains("タイプ")) {
                        retStr = detail;
                    } else if (buf.contains("電源")) {
                        retStr = detail;
                    }
                    break;
                case "fancontroller":
                    if (buf.contains("ファンコントローラ数")) {
                        retStr = "コントローラー数: " + detail;
                    }
                    break;
                case "casefan":
                    if (buf.contains("ファンサイズ")) {
                        retStr = detail;
                    } else if (buf.contains("最大風量")) {
                        retStr = detail;
                    } else if (buf.contains("最大ノイズレベル")) {
                        retStr = detail;
                    } else if (buf.contains("最大回転数")) {
                        retStr = detail;
                    } else if (buf.contains("コネクタ")) {
                        retStr = detail;
                    } else if (buf.contains("LEDライティング対応")) {
                        retStr = "LEDライティング対応";
                    } else if (buf.contains("個数")) {
                        retStr = detail;
                    }
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(s + " exception reason=" + e.getMessage());
            return null;
        }
        return retStr;
    }

    private final List<Integer> core12th_i9 = List.of(200, 240, 300, 110); // normal / K / X / T
    private final List<Integer> core12th_i7 = List.of(180, 190, 280, 100); // normal / K / X / T
    private final List<Integer> core12th_i5 = List.of(120, 150, 120,  80); // normal / K / X / T
    private final List<Integer> core12th_i3 = List.of( 90,  90,  90,  70); // normal / K / X / T
    private final List<Integer> coreUnder11 = List.of( 70, 130, 170,  70); // normal / K / X / T

    private final List<Integer> ryzen9 = List.of( 70, 110,  70,  50,  40,  30,  20); // normal / X / G / H / E / U / C
    private final List<Integer> ryzen7 = List.of( 70, 110,  70,  50,  40,  30,  20); // normal / X / G / H / E / U / C
    private final List<Integer> ryzen5 = List.of( 70, 100,  70,  50,  40,  30,  20); // normal / X / G / H / E / U / C
    private final List<Integer> ryzen3 = List.of( 70,  70,  70,  50,  40,  20,  20); // normal / X / G / H / E / U / C
    private void setCpuPowerConsumption(String cpuName) {
        String str = cpuName.replace("BOX", "");
        int watt = 0;
        List<Integer> cpuPowerList = new ArrayList<>();
        int index = 0;
        if (str.contains("Core") || str.contains("Pentium") || str.contains("Celeron") || str.contains("Atom")) {
            if (str.contains(" 12")) { // 12th
                if (str.contains(" i9")) {
                    cpuPowerList = core12th_i9;
                } else if (str.contains(" i7")) {
                    cpuPowerList = core12th_i7;
                } else if (str.contains(" i5")) {
                    cpuPowerList = core12th_i5;
                } else if (str.contains(" i3")) {
                    cpuPowerList = core12th_i3;
                } else {
                    // pentium, celeron, atom ???
                    watt = 50;
                }
            } else { // 11th or under
                cpuPowerList = coreUnder11;
            }
            if (str.contains("K")) {
                index = 1;
            } else if (str.contains("X")) {
                index = 2;
            } else if (str.contains("T")) {
                index = 3;
            }
        } else if (str.contains("Ryzen") || str.contains("ryzen")) {
            if (str.contains("Ryzen 9")) {
                cpuPowerList = ryzen9;
            } else if (str.contains("Ryzen 7")) {
                cpuPowerList = ryzen7;
            } else if (str.contains("Ryzen 5")) {
                cpuPowerList = ryzen5;
            } else if (str.contains("Ryzen 3")) {
                cpuPowerList = ryzen3;
            } else {
                // other AMD CPUs ???
                watt = 50;
            }
            if (str.contains("X ")) {
                index = 1;
            } else if (str.contains("G ")) {
                index = 2;
            } else if (str.contains("H ") || str.contains("HS ")) {
                index = 3;
            } else if (str.contains("E ") || str.contains("GE ")) {
                index = 4;
            } else if (str.contains("U ")) {
                index = 5;
            } else if (str.contains("C ")) {
                index = 6;
            }
        }

        if (watt == 0) {
            try {
                watt = cpuPowerList.get(index);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("cpuName=" + cpuName + " IndexOutOfBoundsException, reason=" + e.getMessage());
            }
        }
        watt = (int) Math.ceil((double) watt / FLAG1_VOLTAGE_UNIT);
        newFlag1 |= watt << FLAG1_VOLTAGE_SHIFT;
    }
}
