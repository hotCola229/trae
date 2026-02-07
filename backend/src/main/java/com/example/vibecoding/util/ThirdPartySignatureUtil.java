package com.example.vibecoding.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdPartySignatureUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartySignatureUtil.class);
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Asia/Shanghai";
    private static final String CHARSET = "UTF-8";
    private static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";

    /**
     * 生成Timestamp
     * @return 格式化的时间戳
     */
    public static String generateTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_STRING);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return df.format(new Date());
    }

    /**
     * POP特殊的URL编码规则
     * 在一般的URLEncode后再增加三种字符替换：加号（+）替换成 %20、星号（*）替换成 %2A、%7E 替换回波浪号（~）
     */
    public static String specialUrlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, CHARSET)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
     * 构造待签名串
     */
    public static String buildSignString(String method, String url, Map<String, Object> queryParams, 
                                        String appKey, String timestamp) throws UnsupportedEncodingException {
        // 创建参数副本并加入appKey和timestamp
        Map<String, Object> params = new HashMap<>(queryParams);
        params.put("appKey", appKey);
        params.put("timestamp", timestamp);

        // 根据参数Key排序
        TreeMap<String, Object> sortParasMap = new TreeMap<>(params);

        // 把排序后的参数顺序拼接成格式：specialUrlEncode(参数Key) + "=" + specialUrlEncode(参数值)
        StringBuilder sortQueryStringTmp = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortParasMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            sortQueryStringTmp.append("&")
                    .append(specialUrlEncode(key))
                    .append("=")
                    .append(specialUrlEncode(value));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1); // 去除第一个多余的&符号

        // 按POP的签名规则拼接成最终的待签名串
        // 规则：HTTPMethod + "&" + specialUrlEncode(url) + "&" + specialUrlEncode(sortedQueryString)
        return method.toUpperCase() + "&" + specialUrlEncode(url) + "&" + specialUrlEncode(sortedQueryString);
    }

    /**
     * 签名采用HmacSHA1算法 + Base64，编码采用UTF-8
     */
    public static String sign(String appSecret, String stringToSign) throws NoSuchAlgorithmException, 
            InvalidKeyException, UnsupportedEncodingException {
        // 特别说明：POP要求需要后面多加一个"&"字符，即appSecret + "&"
        String signingKey = appSecret + "&";
        SecretKeySpec secretKey = new SecretKeySpec(signingKey.getBytes(CHARSET), ALGORITHM_HMAC_SHA1);
        Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
        mac.init(secretKey);
        byte[] signData = mac.doFinal(stringToSign.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 主入口方法：生成签名
     */
    public static String generateSignature(String httpMethod, String path, Map<String, Object> queryParams, 
                                          String appKey, String appSecret) {
        try {
            String timestamp = generateTimestamp();
            String stringToSign = buildSignString(httpMethod, path, queryParams, appKey, timestamp);
            return sign(appSecret, stringToSign);
        } catch (Exception e) {
            logger.error("Generate signature failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * 生成完整的请求头
     */
    public static Map<String, String> generateHeaders(String appKey, String appSecret, 
                                                     String httpMethod, String path, 
                                                     Map<String, Object> queryParams) {
        String timestamp = generateTimestamp();
        Map<String, String> headers = new HashMap<>();
        headers.put("AppKey", appKey);
        headers.put("Timestamp", timestamp);
        
        try {
            String stringToSign = buildSignString(httpMethod, path, queryParams, appKey, timestamp);
            String signature = sign(appSecret, stringToSign);
            headers.put("Signature", signature);
        } catch (Exception e) {
            logger.error("Generate headers failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate headers", e);
        }
        
        return headers;
    }
}