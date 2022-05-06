package com.kongxn.gps.map;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Data
@ConfigurationProperties(prefix = "baidu")
public class SnCal {

    private String ak;
    private String sk;

    public static String genSn(LinkedHashMap<String, String> paramsMap, String api) throws UnsupportedEncodingException {
        SnCal snCal = new SnCal();
        paramsMap.put("ak",snCal.ak);
        String paramsStr = snCal.toQueryString(paramsMap);
        String wholeStr = new String(api + "?" + paramsStr + snCal.sk);
        return URLEncoder.encode(wholeStr, StandardCharsets.UTF_8);
    }

    // 对Map内所有value作utf8编码，拼接返回结果
    public String toQueryString(Map<?, ?> data)throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey()).append("=");
            queryString.append(URLEncoder.encode((String) pair.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    // 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error(e);
        }
        return null;
    }
}
