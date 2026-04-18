package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Data structure for the Yuewen-4.4 API response (GetConsumeLog).
 */
@Data
public class YuewenConsumeLogResponse {
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * List of consumption items.
     */
    private List<YuewenConsumeItem> list;
}
