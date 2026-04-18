package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request parameters for the Yuewen-4.1 API (GetAppList).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuewenAppListRequest {
    /**
     * Query start timestamp (UNIX timestamp, unit seconds).
     * Optional. Default is today at 00:00:00.
     */
    @JsonProperty("start_time")
    private Long startTime;

    /**
     * Query end timestamp (UNIX timestamp, unit seconds).
     * Optional. Default is current time.
     */
    @JsonProperty("end_time")
    private Long endTime;

    /**
     * Page number.
     * Optional. Default is 1.
     */
    private Integer page;
}
