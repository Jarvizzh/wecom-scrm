package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a single product identification item in the Yuewen API.
 */
@Data
public class YuewenAppFlag {
    /**
     * Product name.
     */
    @JsonProperty("app_name")
    private String appName;

    /**
     * Product unique identifier.
     */
    private String appflag;

    /**
     * Associated WeChat Public Account AppID.
     */
    @JsonProperty("wx_appid")
    private String wxAppid;
}
