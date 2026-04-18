package com.wecom.scrm.thirdparty.yuewen.api.client;

import com.wecom.scrm.WecomScrmServerApplication;
import com.wecom.scrm.thirdparty.api.yuewen.client.YuewenApiClient;
import com.wecom.scrm.thirdparty.api.yuewen.dto.*;
import com.wecom.scrm.thirdparty.yuewen.api.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for YuewenApiClient making real HTTP requests.
 * These tests are disabled by default to avoid running in CI/CD environments without proper credentials.
 */
@Slf4j
@SpringBootTest(classes = WecomScrmServerApplication.class)
@TestPropertySource(locations = "classpath:secret.properties")
public class YuewenApiClientIntegrationTest {

    @Autowired
    private YuewenApiClient apiClient;

    @Test
    //@Disabled("Enable this test to perform a real HTTP request to the GetAppList API")
    void getAppList_RealRequest() {
        YuewenAppListRequest request = YuewenAppListRequest.builder()
                .build();
        
        // Optional: set start/end time if needed
        // request.setStartTime(Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond());
        // request.setEndTime(Instant.now().getEpochSecond());

        YuewenResponse<YuewenAppListResponse> response = apiClient.getAppList(request);

        assertNotNull(response);
        log.info("AppList response: code={}, msg={}", response.getCode(), response.getMsg());
        
        if (response.getCode() == 0) {
            assertNotNull(response.getData());
            log.info("Total apps found: {}", response.getData().getTotalCount());
        } else {
            log.warn("API returned error: {}", response.getMsg());
        }
    }

    @Test
    //@Disabled("Enable this test to perform a real HTTP request to the GetUserInfo API")
    void getUserInfo_RealRequest() {
        // Note: Replace with a valid appflag from getAppList
        String appflag = "wxfx108716"; // Example flag, may need adjustment
        
        YuewenUserInfoRequest request = YuewenUserInfoRequest.builder()
                .appflag(appflag)
                .page(1)
                .build();

        YuewenResponse<YuewenUserInfoResponse> response = apiClient.getUserInfo(request);

        assertNotNull(response);
        log.info("UserInfo response: code={}, msg={}", response.getCode(), response.getMsg());

        if (response.getCode() == 0) {
            assertNotNull(response.getData());
        }
    }

    @Test
    //@Disabled("Enable this test to perform a real HTTP request to the GetRechargeLog API")
    void getRechargeLog_RealRequest() {
        String appflag = "wxfx108716";
        long startTime = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        long endTime = Instant.now().getEpochSecond();

        YuewenRechargeLogRequest request = YuewenRechargeLogRequest.builder()
                .appflag(appflag)
                .startTime(startTime)
                .endTime(endTime)
                .page(1)
                .pageSize(20)
                .build();

        YuewenResponse<YuewenRechargeLogResponse> response = apiClient.getRechargeLog(request);

        assertNotNull(response);
        log.info("RechargeLog response: code={}, msg={}", response.getCode(), response.getMsg());

        if (response.getCode() == 0) {
            assertNotNull(response.getData());
        }
    }

    @Test
    //@Disabled("Enable this test to perform a real HTTP request to the GetConsumeLog API")
    void getConsumeLog_RealRequest() {
        String appflag = "wxfx108716";
        long startTime = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        long endTime = Instant.now().getEpochSecond();

        YuewenConsumeLogRequest request = YuewenConsumeLogRequest.builder()
                .appflag(appflag)
                .startTime(startTime)
                .endTime(endTime)
                .page(1)
                .build();

        YuewenResponse<YuewenConsumeLogResponse> response = apiClient.getConsumeLog(request);

        assertNotNull(response);
        log.info("ConsumeLog response: code={}, msg={}", response.getCode(), response.getMsg());

        if (response.getCode() == 0) {
            assertNotNull(response.getData());
        }
    }
}
