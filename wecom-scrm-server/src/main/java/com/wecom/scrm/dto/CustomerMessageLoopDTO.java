package com.wecom.scrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerMessageLoopDTO {

    @Data
    public static class CreateRequest {
        private String taskName;
        private Integer targetType; // 0: All, 1: Filtered
        private CustomerMessageDTO.TargetCondition targetCondition;
        private String text;
        private List<WecomAttachmentDTO> attachments;
        private List<String> senderList;
        private Integer loopType; // 1: Daily, 2: Weekly
        private String loopDayOfWeek; // e.g. "1,2,3" (1=Mon, 7=Sun)
        private String sendTimeOfDay; // e.g. "10:00:00"
        private Integer status; // 0: Disabled, 1: Enabled
    }

    @Data
    public static class Response {
        private Long id;
        private String taskName;
        private Integer targetType;
        private String text;
        private Integer loopType;
        private String loopDayOfWeek;
        private String sendTimeOfDay;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastTriggerTime;
        private Integer status; // 0: Disabled, 1: Enabled
        private String creatorUserid;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }
}
