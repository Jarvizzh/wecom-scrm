package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request parameters for the Yuewen-4.2 API (GetRechargeLog).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuewenRechargeLogRequest {
    /**
     * Query start timestamp (UNIX timestamp, unit seconds).
     * Mandatory.
     */
    @JsonProperty("start_time")
    private Long startTime;

    /**
     * Query end timestamp (UNIX timestamp, unit seconds).
     * Mandatory.
     */
    @JsonProperty("end_time")
    private Long endTime;

    /**
     * Page number. Default is 1.
     */
    private Integer page;

    /**
     * Page size, max 100. Default is 10.
     */
    @JsonProperty("page_size")
    private Integer pageSize;

    /**
     * Product identifier.
     * Mandatory.
     */
    private String appflag;

    /**
     * Yuewen user ID.
     */
    private Long guid;

    /**
     * User's openid.
     */
    private String openid;

    /**
     * WeChat order ID.
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Order status: 1: Pending, 2: Paid.
     */
    @JsonProperty("order_status")
    private Integer orderStatus;

    /**
     * last_min_id from previous response. Mandatory if page > 1.
     */
    @JsonProperty("last_min_id")
    private Long lastMinId;

    /**
     * last_max_id from previous response. Mandatory if page > 1.
     */
    @JsonProperty("last_max_id")
    private Long lastMaxId;

    /**
     * total_count from previous response. Mandatory if page > 1.
     */
    @JsonProperty("total_count")
    private Integer totalCount;
}
