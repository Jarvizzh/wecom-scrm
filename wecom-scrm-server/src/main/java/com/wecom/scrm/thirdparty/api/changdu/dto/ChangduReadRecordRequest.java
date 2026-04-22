package com.wecom.scrm.thirdparty.api.changdu.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Get user reading record list.
 */
@Data
@Builder
public class ChangduReadRecordRequest {
    private Long distributorId;
    private String deviceId;
    private Long pageIndex;
    private Long pageSize;
    private Long beginTime;
    private Long endTime;
    private String openId;
}
