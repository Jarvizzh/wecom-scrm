package com.wecom.scrm.thirdparty.api.yuewen.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class YuewenSyncRequest {
    private String appFlag;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
