package com.wecom.scrm.thirdparty.api.changdu.util;

import org.apache.commons.codec.digest.DigestUtils;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for generating Changdu API signatures.
 */
public class ChangduSignUtils {

    /**
     * Generates a signature for Changdu GET requests.
     *
     * @param distributorId The distributor identifier.
     * @param secretKey     The secret key for signing.
     * @param ts            Unix timestamp in seconds.
     * @param params        The map of request parameters.
     * @return The MD5 signature in lowercase.
     */
    public static String generateGetSign(String distributorId, String secretKey, long ts, Map<String, Object> params) {
        // 1. Sort parameters by key in ASCII order
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);

        // 2. Concatenate values with '|'
        StringBuilder paramsValue = new StringBuilder();
        sortedParams.forEach((key, value) -> {
            if (value != null) {
                paramsValue.append(value).append("|");
            }
        });

        // 3. Construct final string: distributor_id + secret_key + ts + params_value
        String finalStr = distributorId + secretKey + ts + paramsValue.toString();

        // 4. MD5 and to lowercase
        return DigestUtils.md5Hex(finalStr).toLowerCase();
    }

    /**
     * Generates a signature for Changdu POST requests.
     *
     * @param distributorId The distributor identifier.
     * @param secretKey     The secret key for signing.
     * @param ts            Unix timestamp in seconds.
     * @param jsonBody      The JSON request body.
     * @return The MD5 signature in lowercase.
     */
    public static String generatePostSign(String distributorId, String secretKey, long ts, String jsonBody) {
        // 1. Construct final string: distributor_id + secret_key + ts + jsonBody
        String finalStr = distributorId + secretKey + ts + jsonBody;

        // 2. MD5 and to lowercase
        return DigestUtils.md5Hex(finalStr).toLowerCase();
    }
}
