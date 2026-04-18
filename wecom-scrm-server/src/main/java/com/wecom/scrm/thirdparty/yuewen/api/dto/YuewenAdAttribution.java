package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Nested fields for ad tracking in Yuewen recharge records.
 */
@Data
public class YuewenAdAttribution {
    /**
     * Attribution media site name.
     */
    @JsonProperty("site_label")
    private String siteLabel;

    /**
     * Attribution media site ID.
     */
    private Integer site;

    /**
     * Attribution AID.
     */
    private String aid;
}
