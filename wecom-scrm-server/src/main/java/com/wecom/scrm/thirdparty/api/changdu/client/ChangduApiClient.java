package com.wecom.scrm.thirdparty.api.changdu.client;

import com.wecom.scrm.thirdparty.api.changdu.config.ChangduConfig;
import com.wecom.scrm.thirdparty.api.changdu.dto.*;
import com.wecom.scrm.thirdparty.api.changdu.util.ChangduSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with Changdu API.
 */
@Slf4j
@Component
public class ChangduApiClient implements IChangduApiClient {

    private final ChangduConfig config;
    private final RestTemplate restTemplate;

    public ChangduApiClient(ChangduConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * 3.1 获取账号绑定的分包信息
     *
     * @param request Request parameters.
     * @return API response containing package info list.
     */
    @Override
    public ChangduResponse<List<ChangduPackageInfo>> getPackageInfo(ChangduPackageInfoRequest request) {
        String url = config.getBaseUrl() + "/novelsale/openapi/wx/get_package_list/v2/";

        Map<String, Object> params = new HashMap<>();
        params.put("distributor_id", request.getDistributorId());
        params.put("app_type", request.getAppType());
        if (request.getPageSize() != null) {
            params.put("page_size", request.getPageSize());
        }
        if (request.getPageIndex() != null) {
            params.put("page_index", request.getPageIndex());
        }

        return executeGet(url, String.valueOf(request.getDistributorId()), params,
                new ParameterizedTypeReference<ChangduPackageResponse>() {
                });
    }

    /**
     * 3.2 获取用户信息列表
     *
     * @param request Request parameters.
     * @return API response containing user detail list.
     */
    @Override
    public ChangduResponse<List<ChangduUserDetail>> getUserList(ChangduUserListRequest request) {
        String url = config.getBaseUrl() + "/novelsale/openapi/user/list/v1/";

        Map<String, Object> params = new HashMap<>();
        params.put("distributor_id", request.getDistributorId());
        params.put("page_index", request.getPageIndex());
        params.put("page_size", request.getPageSize());

        if (request.getDeviceId() != null)
            params.put("device_id", request.getDeviceId());
        if (request.getMediaSource() != null)
            params.put("media_source", request.getMediaSource());
        if (request.getBookSource() != null)
            params.put("book_source", request.getBookSource());
        if (request.getPromotionId() != null)
            params.put("promotion_id", request.getPromotionId());
        if (request.getShowNotRecharge() != null)
            params.put("show_not_recharge", request.getShowNotRecharge());
        if (request.getOptimizerAccount() != null)
            params.put("optimizer_account", request.getOptimizerAccount());
        if (request.getBeginTime() != null)
            params.put("begin_time", request.getBeginTime());
        if (request.getEndTime() != null)
            params.put("end_time", request.getEndTime());
        if (request.getOpenId() != null)
            params.put("open_id", request.getOpenId());
        if (request.getExternalId() != null)
            params.put("external_id", request.getExternalId());

        return executeGet(url, String.valueOf(request.getDistributorId()), params,
                new ParameterizedTypeReference<ChangduResponse<List<ChangduUserDetail>>>() {
                });
    }

    /**
     * 3.3 获取用户充值事件
     */
    @Override
    public ChangduResponse<List<ChangduRechargeEvent>> getUserRecharge(ChangduRechargeRequest request) {
        String url = config.getBaseUrl() + "/novelsale/openapi/user/recharge/v1/";

        Map<String, Object> params = new HashMap<>();
        params.put("distributor_id", request.getDistributorId());
        if (request.getBegin() != null)
            params.put("begin", request.getBegin());
        if (request.getEnd() != null)
            params.put("end", request.getEnd());
        if (request.getOffset() != null)
            params.put("offset", request.getOffset());
        if (request.getLimit() != null)
            params.put("limit", request.getLimit());
        if (request.getDeviceId() != null)
            params.put("device_id", request.getDeviceId());
        if (request.getOutsideTradeNo() != null)
            params.put("outside_trade_no", request.getOutsideTradeNo());
        if (request.getPaid() != null)
            params.put("paid", request.getPaid());
        if (request.getOptimizerAccount() != null)
            params.put("optimizer_account", request.getOptimizerAccount());
        if (request.getOpenId() != null)
            params.put("open_id", request.getOpenId());
        if (request.getExternalId() != null)
            params.put("external_id", request.getExternalId());
        if (request.getOrderType() != null)
            params.put("order_type", request.getOrderType());
        if (request.getIsAutoRenewOrder() != null)
            params.put("is_auto_renew_order", request.getIsAutoRenewOrder());
        if (request.getPromotionId() != null)
            params.put("promotion_id", request.getPromotionId());

        return executeGet(url, String.valueOf(request.getDistributorId()), params,
                new ParameterizedTypeReference<ChangduResponse<List<ChangduRechargeEvent>>>() {
                });
    }

    /**
     * 3.4 获取用户消费记录
     */
    @Override
    public ChangduResponse<List<ChangduPurchaseRecord>> getUserPurchase(ChangduPurchaseRequest request) {
        String url = config.getBaseUrl() + "/novelsale/openapi/user/purchase/v1/";

        Map<String, Object> params = new HashMap<>();
        params.put("distributor_id", request.getDistributorId());
        if (request.getBegin() != null)
            params.put("begin", request.getBegin());
        if (request.getEnd() != null)
            params.put("end", request.getEnd());
        if (request.getOffset() != null)
            params.put("offset", request.getOffset());
        if (request.getLimit() != null)
            params.put("limit", request.getLimit());
        if (request.getDeviceId() != null)
            params.put("device_id", request.getDeviceId());
        if (request.getOutsideTradeNo() != null)
            params.put("outside_trade_no", request.getOutsideTradeNo());
        if (request.getPaid() != null)
            params.put("paid", request.getPaid());
        if (request.getOpenId() != null)
            params.put("open_id", request.getOpenId());
        if (request.getExternalId() != null)
            params.put("external_id", request.getExternalId());

        return executeGet(url, String.valueOf(request.getDistributorId()), params,
                new ParameterizedTypeReference<ChangduResponse<List<ChangduPurchaseRecord>>>() {
                });
    }

    /**
     * 3.5 获取用户阅读记录列表
     */
    @Override
    public ChangduResponse<List<ChangduReadRecord>> getUserReadList(ChangduReadRecordRequest request) {
        String url = config.getBaseUrl() + "/novelsale/openapi/user/read/list/v1/";

        Map<String, Object> params = new HashMap<>();
        params.put("distributor_id", request.getDistributorId());
        params.put("device_id", request.getDeviceId());
        params.put("page_index", request.getPageIndex());
        params.put("page_size", request.getPageSize());

        if (request.getBeginTime() != null)
            params.put("begin_time", request.getBeginTime());
        if (request.getEndTime() != null)
            params.put("end_time", request.getEndTime());
        if (request.getOpenId() != null)
            params.put("open_id", request.getOpenId());

        return executeGet(url, String.valueOf(request.getDistributorId()), params,
                new ParameterizedTypeReference<ChangduResponse<List<ChangduReadRecord>>>() {
                });
    }

    private <T> T executeGet(String url, String distributorId, Map<String, Object> params,
            ParameterizedTypeReference<T> responseType) {
        long ts = System.currentTimeMillis() / 1000;
        String sign = ChangduSignUtils.generateGetSign(distributorId, config.getSecretKey(), ts, params);
        log.info("ts: {} sign:{}", ts, sign);

        HttpHeaders headers = new HttpHeaders();
        headers.set("header-ts", String.valueOf(ts));
        headers.set("header-sign", sign);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach(builder::queryParam);

        String finalUrl = builder.toUriString();
        log.info("Calling Changdu API: {}", finalUrl);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call Changdu API: {}", url, e);
            throw new RuntimeException("Changdu API call failed: " + e.getMessage(), e);
        }
    }
}
