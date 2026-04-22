package com.wecom.scrm.thirdparty.api.changdu.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Get user purchase records.
 */
@Data
@Builder
public class ChangduPurchaseRequest {
    private Long distributorId;
    private Long begin;
    private Long end;
    private Integer offset;
    private Integer limit;
    private String deviceId;
    private String outsideTradeNo;
    private Boolean paid;
    private String openId;
    private String externalId;
    private Integer orderType;
    private Boolean isAutoRenewOrder;
    private Long promotionId;
}
