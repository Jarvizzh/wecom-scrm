package com.wecom.scrm.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerMessageDTO {

    @Data
    public static class CreateRequest {
        private String taskName;
        private Integer sendType; // 0: Immediate, 1: Scheduled
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime sendTime;
        private Integer targetType; // 0: All, 1: Filtered
        private TargetCondition targetCondition;
        private String text;
        private List<MomentDTO.Attachment> attachments;
        private List<String> senderList;
    }

    @Data
    public static class TargetCondition {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime addTimeStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime addTimeEnd;
        private List<String> includeTags; // Tag IDs
        private Boolean includeTagsAny; // true: ANY, false: ALL
        private List<String> excludeTags; // Tag IDs
    }

    @Data
    public static class Response {
        private Long id;
        private String taskName;
        private Integer sendType;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime sendTime;
        private Integer targetType;
        private String text;
        private Integer status; // 0: Pending, 1: Sending, 2: Finished, 3: Failed
        private String creatorUserid;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        private Integer targetCount;
    }

    @Data
    public static class SendResult {
        private String nextCursor;
        private List<MemberTask> taskList;
    }

    @Data
    public static class MemberTask {
        private String userId;
        private String userName;
        private Integer status; // 0: Unsent, 1: Sent, 2: Deleted/Exited
        private Long sendTime;
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
    }

    @Data
    public static class MemberSendDetail {
        private String userId;
        private String userName;
        private List<CustomerResult> customerList;
    }

    @Data
    public static class CustomerResult {
        @JsonAlias({"external_userid", "externalUserId"})
        private String externalUserid;
        private String customerName;
        private Integer status; // 0: Unsent, 1: Sent, 2: Failed/Deleted, 3:Receive Limited
        private Long sendTime;
        private String failReason;
    }
}
