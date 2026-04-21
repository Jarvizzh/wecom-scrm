package com.wecom.scrm.thirdparty.api.changdu.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Get user information list.
 */
@Data
@Builder
public class ChangduUserListRequest {
    /**
     * 分销商id (必填)
     */
    private Long distributorId;
    
    /**
     * 页码，从0开始 (必填)
     */
    private Long pageIndex;
    
    /**
     * 一页多少数据，默认10，最大1000 (必填)
     */
    private Long pageSize;
    
    /**
     * 加密的设备ID (可选)
     */
    private String deviceId;
    
    /**
     * 媒体渠道 (可选)
     */
    private Integer mediaSource;
    
    /**
     * 书籍来源 (可选)
     */
    private String bookSource;
    
    /**
     * 推广链ID (可选)
     */
    private String promotionId;
    
    /**
     * 传True获取所有用户，false/不传只展示充值用户 (可选)
     */
    private Boolean showNotRecharge = Boolean.TRUE;
    
    /**
     * 投手账号 (可选)
     */
    private String optimizerAccount;
    
    /**
     * 开始时间 (Unix 时间戳) (可选)
     */
    private Long beginTime;
    
    /**
     * 结束时间 (Unix 时间戳) (可选)
     */
    private Long endTime;
    
    /**
     * 用户open_id，微信H5场景 (可选)
     */
    private String openId;
    
    /**
     * 企微用户企微id，微信H5场景 (可选)
     */
    private String externalId;
}
