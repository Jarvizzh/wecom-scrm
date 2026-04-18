package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request parameters for the Yuewen-4.4 API (GetConsumeLog).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuewenConsumeLogRequest {
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
     * User's openid. (Mandatory if guid is not provided)
     */
    private String openid;

    /**
     * Yuewen user ID. (Mandatory if openid is not provided)
     */
    private Long guid;
}
