package jp.developer.bbee.pcassem;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StringEncoder {

    /**
     * 引数の文字列(Shift_JIS)を、UTF-8にエンコードする。
     *
     * @param value 変換対象の文字列
     * @return エンコードされた文字列
     */
    public static String sjisToUtf8(String value) throws UnsupportedEncodingException {
        byte[] srcStream = value.getBytes("SJIS");
        byte[] destStream = (new String(srcStream, "SJIS")).getBytes("UTF-8");
        value = new String(destStream, "UTF-8");
        value = StringEncoder.convert(value, "SJIS", "UTF-8");
        return value;
    }

    /**
     * 引数の文字列(UTF-8)を、Shift_JISにエンコードする。
     *
     * @param value 変換対象の文字列
     * @return エンコードされた文字列
     */
    public static String utf8ToSjis(String value) throws UnsupportedEncodingException {
        byte[] srcStream = value.getBytes("UTF-8");
        value = convert(new String(srcStream, "UTF-8"), "UTF-8", "SJIS");
        byte[] destStream = value.getBytes("SJIS");
        value = new String(destStream, "SJIS");
        return value;
    }

    /**
     * 引数の文字列を、エンコードする。
     *
     * @param value 変換対象の文字列
     * @param src 変換前の文字コード
     * @param dest 変換後の文字コード
     * @return エンコードされた文字列
     */
    private static String convert(String value, String src, String dest) throws UnsupportedEncodingException {
        Map<String, String> conversion = createConversionMap(src, dest);
        char oldChar;
        char newChar;
        String key;
        for (Iterator<String> itr = conversion.keySet().iterator(); itr.hasNext() ;) {
            key = itr.next();
            oldChar = toChar(key);
            newChar = toChar(conversion.get(key));
            value = value.replace(oldChar, newChar);
        }
        return value;
    }

    /**
     * エンコード情報を作成する
     *
     * @param src 変換前の文字コード
     * @param dest 変換後の文字コード
     * @return エンコードされた文字列
     */
    private static Map<String, String> createConversionMap(String src, String dest) throws UnsupportedEncodingException {
        Map<String, String> conversion = new HashMap<String, String>();
        if ((src.equals("UTF-8")) && (dest.equals("SJIS"))) {
            // －（全角マイナス）
            conversion.put("U+FF0D", "U+2212");
            // ～（全角チルダ）
            conversion.put("U+FF5E", "U+301C");
            // ￠（セント）
            conversion.put("U+FFE0", "U+00A2");
            // ￡（ポンド）
            conversion.put("U+FFE1", "U+00A3");
            // ￢（ノット）
            conversion.put("U+FFE2", "U+00AC");
            // ―（全角マイナスより少し幅のある文字）
            conversion.put("U+2015", "U+2014");
            // ∥（半角パイプが2つ並んだような文字）
            conversion.put("U+2225", "U+2016");

        } else if ((src.equals("SJIS")) && (dest.equals("UTF-8"))) {
            // －（全角マイナス）
            conversion.put("U+2212", "U+FF0D");
            // ～（全角チルダ）
            conversion.put("U+301C", "U+FF5E");
            // ￠（セント）
            conversion.put("U+00A2", "U+FFE0");
            // ￡（ポンド）
            conversion.put("U+00A3", "U+FFE1");
            // ￢（ノット）
            conversion.put("U+00AC", "U+FFE2");
            // ―（全角マイナスより少し幅のある文字）
            conversion.put("U+2014", "U+2015");
            // ∥（半角パイプが2つ並んだような文字）
            conversion.put("U+2016", "U+2225");

        } else {
            throw new UnsupportedEncodingException("この文字コードはサポートしていません。\n・src=" + src + ",dest=" + dest);
        }
        return conversion;
    }

    /**
     * 16進表記の文字を取得する。
     *
     * @param value 変換対象の文字列
     * @return 16進表記の文字
     */
    private static char toChar(String value) {
        return (char)Integer.parseInt(value.trim().substring("U+".length()), 16);
    }
}