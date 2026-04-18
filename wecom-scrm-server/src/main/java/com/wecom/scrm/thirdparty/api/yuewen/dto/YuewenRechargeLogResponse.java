package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Data structure for the Yuewen-4.2 API response (GetRechargeLog).
 */
@Data
public class YuewenRechargeLogResponse {
    private Integer page;

    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * Minimum ID used for cursor-based pagination.
     */
    @JsonProperty("min_id")
    private Long minId;

    /**
     * Maximum ID used for cursor-based pagination.
     */
    @JsonProperty("max_id")
    private Long maxId;

    /**
     * List of recharge items.
     */
    private List<YuewenRechargeItem> list;
}
