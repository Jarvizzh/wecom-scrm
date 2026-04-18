package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Data structure for the Yuewen-4.3 API response (GetUserInfo).
 */
@Data
public class YuewenUserInfoResponse {
    /**
     * Next page ID for cursor-based pagination.
     */
    @JsonProperty("next_id")
    private String nextId;

    /**
     * Total record count.
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * List of user info items.
     */
    private List<YuewenUserItem> list;
}
