package com.wecom.scrm.dto;

import lombok.Data;
import java.util.List;

@Data
public class MomentDTO {

    @Data
    public static class CreateRequest {
        private String taskName;
        private String text;
        private List<WecomAttachmentDTO> attachments;
        private VisibleRange visibleRange;
        private Integer sendType; // 0: Immediate, 1: Scheduled
        private String sendTime; // YYYY-MM-DD HH:mm:ss
    }

    @Data
    public static class VisibleRange {
        private List<String> senderList;
        private List<Long> departmentList;
        private ExternalContactList externalContactList;
    }

    @Data
    public static class ExternalContactList {
        private List<String> tagList;
    }

    @Data
    public static class Response {
        private Long id;
        private String momentId;
        private String text;
        private Integer status;
        private String createTime;
    }
}
