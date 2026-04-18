package com.wecom.scrm.thirdparty.api.yuewen.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for generating Yuewen API signatures.
 */
public class YuewenSignUtils {

    /**
     * Generates a signature for Yuewen API requests.
     *
     * @param params    The map of request parameters (common + business, excluding sign).
     * @param appSecret The partner's app secret.
     * @return The character-encoded MD5 signature in uppercase.
     */
    public static String generateSign(Map<String, String> params, String appSecret) {
        // 1. Sort parameters by key in ASCII order
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2 & 3. Concatenate appSecret + key1value1key2value2...
        StringBuilder sb = new StringBuilder(appSecret);
        sortedParams.forEach((key, value) -> {
            if (value != null) {
                sb.append(key).append(value);
            }
        });

        // 4. MD5 and to uppercase
        return DigestUtils.md5Hex(sb.toString()).toUpperCase();
    }
}
