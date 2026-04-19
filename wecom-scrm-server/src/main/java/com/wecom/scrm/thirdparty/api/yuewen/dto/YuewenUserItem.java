package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Represents a single user info record in the Yuewen-4.3 API.
 */
@Data
@ToString
public class YuewenUserItem {
    /**
     * Cumulative recharge amount (unit: cent).
     */
    @JsonProperty("charge_amount")
    private Long chargeAmount;

    /**
     * Cumulative recharge count.
     */
    @JsonProperty("charge_num")
    private Integer chargeNum;

    /**
     * Yuewen user ID.
     */
    private Long guid;

    /**
     * User's openid.
     */
    private String openid;

    private String nickname;

    /**
     * User gender: 1: Male, 2: Female, 0: Unknown.
     */
    private Integer sex;

    /**
     * Whether the user followed the public account: 1: Yes, 0: No.
     */
    @JsonProperty("is_subscribe")
    private Integer isSubscribe;

    private String appflag;

    /**
     * Mapped product app name.
     */
    @JsonProperty("area_name")
    private String areaName;

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("subscribe_time")
    private String subscribeTime;

    @JsonProperty("vip_end_time")
    private String vipEndTime;

    @JsonProperty("update_time")
    private String updateTime;

    /**
     * Sequence time.
     */
    @JsonProperty("seq_time")
    private String seqTime;

    /**
     * Reflux time.
     */
    @JsonProperty("reflux_time")
    private String refluxTime;

    @JsonProperty("channel_id")
    private Long channelId;

    @JsonProperty("original_channel_id")
    private String originalChannelId;

    @JsonProperty("channel_name")
    private String channelName;

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("book_name")
    private String bookName;

    /**
     * OS version name (e.g., android, iOS).
     */
    @JsonProperty("os_type")
    private String osType;

    /**
     * Device manufacturer.
     */
    private String manufacturer;

    /**
     * External user information list.
     */
    @JsonProperty("external_userinfo")
    private List<YuewenExternalUserInfo> externalUserInfo;
}
