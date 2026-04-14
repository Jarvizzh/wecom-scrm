package com.wecom.scrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupMessageDTO {

    @Data
    public static class CreateRequest {
        private String taskName;
        private Integer sendType; // 0: Immediate, 1: Scheduled
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime sendTime;
        private Integer targetType; // 0: All Groups, 1: Filtered Groups
        private TargetCondition targetCondition;
        private String text;
        private List<MomentDTO.Attachment> attachments;
    }

    @Data
    public static class TargetCondition {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeStart; // Group chat creation time range start
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeEnd;   // Group chat creation time range end
        private List<String> groupNameKeywords; // Group name contains any of these keywords
        private List<String> ownerUserids;     // Filter by group owners (staff)
    }

    @Data
    public static class SendResult {
        private List<GroupSendRecord> groupList;
    }

    @Data
    public static class GroupSendRecord {
        private String chatId;
        private String groupName;
        private Integer status; // 0: Unsent, 1: Sent, 2: Failed
        private Long sendTime;
    }
}
