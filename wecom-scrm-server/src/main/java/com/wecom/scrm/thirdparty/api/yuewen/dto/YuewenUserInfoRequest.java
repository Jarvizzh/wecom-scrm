package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request parameters for the Yuewen-4.3 API (GetUserInfo).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuewenUserInfoRequest {
    /**
     * Query start timestamp (UNIX timestamp, unit seconds).
     */
    @JsonProperty("start_time")
    private Long startTime;

    /**
     * Query end timestamp (UNIX timestamp, unit seconds).
     */
    @JsonProperty("end_time")
    private Long endTime;

    /**
     * Page number. Default is 1.
     */
    private Integer page;

    /**
     * Product identifier.
     * Mandatory.
     */
    private String appflag;

    /**
     * User's openid.
     */
    private String openid;

    /**
     * next_id from previous response. Mandatory if page > 1.
     */
    @JsonProperty("next_id")
    private String nextId;
}
