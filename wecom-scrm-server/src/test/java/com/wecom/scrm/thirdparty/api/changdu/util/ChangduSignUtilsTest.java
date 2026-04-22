package com.wecom.scrm.thirdparty.api.changdu.util;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangduSignUtilsTest {

    @Test
    public void testGenerateGetSign() {
        String distributorId = "1811111111";
        String secretKey = "ZMSa111111111111";
        long ts = 1618367023L;
        
        Map<String, Object> params = new HashMap<>();
        params.put("page_index", 0);
        params.put("page_size", 50);
        params.put("distributor_id", 1811111111);

        // Sorting: distributor_id, page_index, page_size
        // Values: 1811111111|0|50|
        // String to sign: 1811111111ZMSa11111111111116183670231811111111|0|50|
        
        String sign = ChangduSignUtils.generateGetSign(distributorId, secretKey, ts, params);
        assertEquals("036f7269d6b7b8e1c9b1853a5af692b6", sign);
    }

    @Test
    public void testGeneratePostSign() {
        String distributorId = "1811111111";
        String secretKey = "ZMSa111111111111";
        long ts = 1618367023L;
        String jsonBody = "{\"book_id\":\"111\"}";

        String sign = ChangduSignUtils.generatePostSign(distributorId, secretKey, ts, jsonBody);
        assertEquals("9ccf5dbd2391537b7086cc1aa266e12a", sign);
    }
}
