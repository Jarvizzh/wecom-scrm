package com.wecom.scrm.thirdparty.yuewen.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class YuewenSyncRequest {
    private String appFlag;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
