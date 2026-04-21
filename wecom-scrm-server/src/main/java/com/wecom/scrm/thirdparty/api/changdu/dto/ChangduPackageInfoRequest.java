package com.wecom.scrm.thirdparty.api.changdu.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Get account-bound package info.
 */
@Data
@Builder
public class ChangduPackageInfoRequest {
    /**
     * 分销ID (必填)
     */
    private Long distributorId;

    /**
     * 应用类型
     */
    private Integer appType;
    
    /**
     * 分页大小 (可选)
     */
    private Integer pageSize;
    
    /**
     * 分页索引 (可选)
     */
    private Integer pageIndex;
}
