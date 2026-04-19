package com.wecom.scrm.thirdparty.api.yuewen.client;

import com.wecom.scrm.thirdparty.api.yuewen.config.YuewenConfig;
import com.wecom.scrm.thirdparty.api.yuewen.dto.*;
import com.wecom.scrm.thirdparty.api.yuewen.util.YuewenSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with Yuewen API.
 */
@Slf4j
@Component
public class YuewenApiClient implements IYuewenAPIClient {

    private final YuewenConfig config;
    private final RestTemplate restTemplate;

    public YuewenApiClient(YuewenConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * 4.1 获取产品标识列表（appflag）
     *
     * @param request Request parameters (start_time, end_time, page).
     * @return API response containing the app list.
     */
    @Override
    public YuewenResponse<YuewenAppListResponse> getAppList(YuewenAppListRequest request) {
        String url = config.getBaseUrl() + "/wechat/GetAppList";

        Map<String, String> params = prepareCommonParams();
        if (request.getStartTime() != null) {
            params.put("start_time", String.valueOf(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            params.put("end_time", String.valueOf(request.getEndTime()));
        }
        if (request.getPage() != null) {
            params.put("page", String.valueOf(request.getPage()));
        }

        return executeGet(url, params, new ParameterizedTypeReference<YuewenResponse<YuewenAppListResponse>>() {});
    }

    /**
     * 4.2 获取充值记录
     *
     * @param request Request parameters.
     * @return API response containing recharge logs.
     */
    @Override
    public YuewenResponse<YuewenRechargeLogResponse> getRechargeLog(YuewenRechargeLogRequest request) {
        String url = config.getBaseUrl() + "/wechat/Recharge/GetRechargeLog";

        Map<String, String> params = prepareCommonParams();
        params.put("appflag", request.getAppflag());
        params.put("start_time", String.valueOf(request.getStartTime()));
        params.put("end_time", String.valueOf(request.getEndTime()));

        if (request.getPage() != null) {
            params.put("page", String.valueOf(request.getPage()));
        }
        if (request.getPageSize() != null) {
            params.put("page_size", String.valueOf(request.getPageSize()));
        }
        if (request.getGuid() != null) {
            params.put("guid", String.valueOf(request.getGuid()));
        }
        if (request.getOpenid() != null) {
            params.put("openid", request.getOpenid());
        }
        if (request.getOrderId() != null) {
            params.put("order_id", request.getOrderId());
        }
        if (request.getOrderStatus() != null) {
            params.put("order_status", String.valueOf(request.getOrderStatus()));
        }
        if (request.getLastMinId() != null) {
            params.put("last_min_id", String.valueOf(request.getLastMinId()));
        }
        if (request.getLastMaxId() != null) {
            params.put("last_max_id", String.valueOf(request.getLastMaxId()));
        }
        if (request.getTotalCount() != null) {
            params.put("total_count", String.valueOf(request.getTotalCount()));
        }

        return executeGet(url, params, new ParameterizedTypeReference<YuewenResponse<YuewenRechargeLogResponse>>() {});
    }

    /**
     * 4.3 获取用户信息
     *
     * @param request Request parameters.
     * @return API response containing user info.
     */
    @Override
    public YuewenResponse<YuewenUserInfoResponse> getUserInfo(YuewenUserInfoRequest request) {
        String url = config.getBaseUrl() + "/wechat/User/GetUserInfo";

        Map<String, String> params = prepareCommonParams();
        params.put("appflag", request.getAppflag());

        if (request.getStartTime() != null) {
            params.put("start_time", String.valueOf(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            params.put("end_time", String.valueOf(request.getEndTime()));
        }
        if (request.getPage() != null) {
            params.put("page", String.valueOf(request.getPage()));
        }
        if (request.getOpenid() != null) {
            params.put("openid", request.getOpenid());
        }
        if (request.getNextId() != null) {
            params.put("next_id", request.getNextId());
        }

        return executeGet(url, params, new ParameterizedTypeReference<YuewenResponse<YuewenUserInfoResponse>>() {});
    }

    /**
     * 4.4 获取消费记录
     *
     * @param request Request parameters.
     * @return API response containing consumption logs.
     */
    @Override
    public YuewenResponse<YuewenConsumeLogResponse> getConsumeLog(YuewenConsumeLogRequest request) {
        String url = config.getBaseUrl() + "/wechat/Recharge/GetConsumeLog";

        Map<String, String> params = prepareCommonParams();
        params.put("appflag", request.getAppflag());

        if (request.getStartTime() != null) {
            params.put("start_time", String.valueOf(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            params.put("end_time", String.valueOf(request.getEndTime()));
        }
        if (request.getPage() != null) {
            params.put("page", String.valueOf(request.getPage()));
        }
        if (request.getOpenid() != null) {
            params.put("openid", request.getOpenid());
        }
        if (request.getGuid() != null) {
            params.put("guid", String.valueOf(request.getGuid()));
        }

        return executeGet(url, params, new ParameterizedTypeReference<YuewenResponse<YuewenConsumeLogResponse>>() {});
    }

    private Map<String, String> prepareCommonParams() {
        Map<String, String> params = new HashMap<>();
        params.put("email", config.getEmail());
        params.put("version", String.valueOf(config.getVersion()));
        params.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        return params;
    }

    private <T> YuewenResponse<T> executeGet(String url, Map<String, String> params, ParameterizedTypeReference<YuewenResponse<T>> responseType) {
        // Generate signature
        String sign = YuewenSignUtils.generateSign(params, config.getAppSecret());
        params.put("sign", sign);

        if (log.isDebugEnabled()) {
            log.debug("appSecret:{} params:{} sign:{}", config.getAppSecret(), params, sign);
        }

        // Build URI with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach(builder::queryParam);

        String finalUrl = builder.toUriString();
        log.info("Calling Yuewen API: {}", finalUrl);

        try {
            ResponseEntity<YuewenResponse<T>> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    null,
                    responseType
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call Yuewen API: {}", url, e);
            YuewenResponse<T> errorResponse = new YuewenResponse<>();
            errorResponse.setCode(-1);
            errorResponse.setMsg("Request failed: " + e.getMessage());
            return errorResponse;
        }
    }
}
