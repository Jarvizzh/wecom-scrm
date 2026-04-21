package com.wecom.scrm.thirdparty.api.changdu.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Get user recharge events.
 */
@Data
@Builder
public class ChangduRechargeRequest {
    private Long distributorId;
    private Long begin;
    private Long end;
    private Integer offset;
    private Integer limit;
    private String deviceId;
    private String outsideTradeNo;
    private Boolean paid;
    private String optimizerAccount;
    private String openId;
    private String externalId;
    private Integer orderType; //1 虚拟支付，2 非虚拟支付
    private Boolean isAutoRenewOrder;
    private Long promotionId;
}
