package com.wecom.scrm.thirdparty.api.changdu.client;

import com.wecom.scrm.WecomScrmServerApplication;
import com.wecom.scrm.thirdparty.api.changdu.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for ChangduApiClient making real HTTP requests.
 */
@Slf4j
@SpringBootTest(classes = WecomScrmServerApplication.class)
@TestPropertySource(locations = "classpath:secret.properties")
public class ChangduApiClientIntegrationTest {

    @Autowired
    private IChangduApiClient apiClient;

    @Value("${changdu.api.distributor-id}")
    private String distributorIdStr;
    @Value("${changdu.api.app-type}")
    private String appType;

    @Test
    void getPackageInfo_RealRequest() {
        Long distributorId = Long.valueOf(distributorIdStr);
        ChangduPackageInfoRequest request = ChangduPackageInfoRequest.builder()
                .appType(Integer.valueOf(appType))
                .distributorId(distributorId)
                .pageIndex(0)
                .pageSize(50)
                .build();

        ChangduResponse<List<ChangduPackageInfo>> response = apiClient.getPackageInfo(request);

        assertNotNull(response);
        log.info("PackageInfo response: code={}, message={}, total={}, result={}", 
                response.getCode(), response.getMessage(), response.getTotal(), response.getResult());
    }

    @Test
    void getUserList_RealRequest() {
        Long distributorId = Long.valueOf(distributorIdStr);
        ChangduUserListRequest request = ChangduUserListRequest.builder()
                .distributorId(distributorId)
                .pageIndex(0L)
                .pageSize(10L)
                .build();

        ChangduResponse<List<ChangduUserDetail>> response = apiClient.getUserList(request);

        assertNotNull(response);
        log.info("UserList response: code={}, message={}, total={}, data={}", 
                response.getCode(), response.getMessage(), response.getTotal(), response.getData());
    }

    @Test
    void getUserRecharge_RealRequest() {
        Long distributorId = Long.valueOf(distributorIdStr);
        long begin = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        long end = Instant.now().getEpochSecond();

        ChangduRechargeRequest request = ChangduRechargeRequest.builder()
                .distributorId(distributorId)
                .begin(begin)
                .end(end)
                .limit(10)
                .offset(0)
                .build();

        ChangduResponse<List<ChangduRechargeEvent>> response = apiClient.getUserRecharge(request);

        assertNotNull(response);
        log.info("UserRecharge response: code={}, message={}, hasMore={}, nextOffset={}, result={}", 
                response.getCode(), response.getMessage(), response.getHasMore(), response.getNextOffset(), response.getResult());
    }

    @Test
    void getUserPurchase_RealRequest() {
        Long distributorId = Long.valueOf(distributorIdStr);
        long begin = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        long end = Instant.now().getEpochSecond();

        ChangduPurchaseRequest request = ChangduPurchaseRequest.builder()
                .distributorId(distributorId)
                .begin(begin)
                .end(end)
                .limit(10)
                .offset(0)
                .build();

        ChangduResponse<List<ChangduPurchaseRecord>> response = apiClient.getUserPurchase(request);

        assertNotNull(response);
        log.info("UserPurchase response: code={}, message={}, result={}", 
                response.getCode(), response.getMessage(), response.getResult());
    }

    @Test
    void getUserReadList_RealRequest() {
        Long distributorId = Long.valueOf(distributorIdStr);
        ChangduReadRecordRequest request = ChangduReadRecordRequest.builder()
                .distributorId(distributorId)
                .deviceId("dummy_device")
                .pageIndex(0L)
                .pageSize(10L)
                .build();

        ChangduResponse<List<ChangduReadRecord>> response = apiClient.getUserReadList(request);

        assertNotNull(response);
        log.info("UserReadList response: code={}, message={}, data={}", 
                response.getCode(), response.getMessage(), response.getData());
    }
}
