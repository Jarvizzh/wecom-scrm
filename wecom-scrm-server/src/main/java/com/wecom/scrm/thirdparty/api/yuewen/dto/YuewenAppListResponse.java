package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Data structure for the Yuewen-4.1 API response.
 */
@Data
public class YuewenAppListResponse {
    /**
     * Current page number.
     */
    private Integer page;

    /**
     * Total record count.
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * List of appflags.
     */
    private List<YuewenAppFlag> list;
}
