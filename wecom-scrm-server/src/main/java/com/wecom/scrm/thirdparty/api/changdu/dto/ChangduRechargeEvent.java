package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Changdu User Recharge Action (Recharge Event).
 */
@Data
public class ChangduRechargeEvent {
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    
    @JsonProperty("pay_way")
    private String payWay;
    
    @JsonProperty("pay_fee")
    private Long payFee;
    
    private String status;
    
    @JsonProperty("create_time")
    private String createTime;
    
    @JsonProperty("pay_time")
    private String payTime;
    
    @JsonProperty("promotion_id")
    private Long promotionId;
    
    @JsonProperty("book_id")
    private Long bookId;
    
    @JsonProperty("is_activity")
    private Boolean isActivity;
    
    @JsonProperty("trade_no")
    private Long tradeNo;
    
    @JsonProperty("open_id")
    private String openId;
    
    @JsonProperty("register_time")
    private String registerTime;
    
    @JsonProperty("recent_read_book_id")
    private String recentReadBookId;
    
    @JsonProperty("external_id")
    private String externalId;
    
    @JsonProperty("order_type")
    private Integer orderType;  //订单类型 1 虚拟支付，2 非虚拟支付
    
    @JsonProperty("wx_video_id")
    private String wxVideoId;
    
    @JsonProperty("wx_video_channel_id")
    private String wxVideoChannelId;
    
    @JsonProperty("wx_promotion_id")
    private String wxPromotionId;
    
    @JsonProperty("wx_vc_source_type")
    private Integer wxVcSourceType;
    
    @JsonProperty("is_auto_renew_order")
    private Boolean isAutoRenewOrder;
    
    @JsonProperty("book_name")
    private String bookName;
    
    @JsonProperty("recharge_type")
    private Integer rechargeType;
}
