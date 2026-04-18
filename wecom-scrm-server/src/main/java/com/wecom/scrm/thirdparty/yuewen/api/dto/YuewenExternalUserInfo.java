package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Mapping for external systems in Yuewen user info.
 */
@Data
public class YuewenExternalUserInfo {
    /**
     * Partner institution ID.
     */
    @JsonProperty("corp_id")
    private String corpId;

    /**
     * Customer service ID.
     */
    @JsonProperty("kf_id")
    private String kfId;

    /**
     * External user identifier.
     */
    @JsonProperty("external_userid")
    private String externalUserId;
}
