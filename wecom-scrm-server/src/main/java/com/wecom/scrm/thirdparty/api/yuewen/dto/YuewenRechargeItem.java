package com.wecom.scrm.thirdparty.api.yuewen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Represents a single recharge record item in the Yuewen-4.2 API.
 */
@Data
public class YuewenRechargeItem {
    @JsonProperty("app_name")
    private String appName;

    private String appflag;

    /**
     * Recharge amount (unit: Yuan).
     */
    private String amount;

    /**
     * Yuewen order ID.
     */
    @JsonProperty("yworder_id")
    private String ywOrderId;

    /**
     * WeChat pay order ID.
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Order creation time.
     */
    @JsonProperty("order_time")
    private String orderTime;

    /**
     * Order payment time (success time).
     */
    @JsonProperty("pay_time")
    private String payTime;

    /**
     * Order status: 1: Pending, 2: Paid.
     */
    @JsonProperty("order_status")
    private Integer orderStatus;

    /**
     * Order type: 1: Recharge, 2: Yearly subscription, 3: Props.
     */
    @JsonProperty("order_type")
    private Integer orderType;

    /**
     * User's openid.
     */
    private String openid;

    /**
     * Yuewen user ID.
     */
    private Long guid;

    private String nickname;

    /**
     * User gender: 1: Male, 2: Female, 0: Unknown.
     */
    private Integer sex;

    @JsonProperty("reg_time")
    private String regTime;

    /**
     * Latest follow time.
     */
    @JsonProperty("sub_time")
    private String subTime;

    @JsonProperty("channel_id")
    private Long channelId;

    @JsonProperty("original_channel_id")
    private String originalChannelId;

    @JsonProperty("channel_name")
    private String channelName;

    @JsonProperty("inner_channel_id")
    private Long innerChannelId;

    @JsonProperty("inner_channel_name")
    private String innerChannelName;

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("book_name")
    private String bookName;

    @JsonProperty("wx_appid")
    private String wxAppid;

    /**
     * Media platform report status.
     */
    @JsonProperty("report_status")
    private Integer reportStatus;

    @JsonProperty("item_name")
    private String itemName;

    /**
     * Recharge channel: 1: Alipay, 2: WeChat, 28: TikTok.
     */
    @JsonProperty("order_channel")
    private Integer orderChannel;

    /**
     * Ad attributions list.
     */
    @JsonProperty("ad_attributions")
    private List<YuewenAdAttribution> adAttributions;
}
