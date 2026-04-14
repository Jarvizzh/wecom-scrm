package com.wecom.scrm.dto;

import lombok.Data;
import java.util.List;

@Data
public class MomentDTO {

    @Data
    public static class CreateRequest {
        private String text;
        private List<Attachment> attachments;
        private VisibleRange visibleRange;
    }

    @Data
    public static class Attachment {
        private String msgtype; // image, video, link, miniprogram
        private Image image;
        private Video video;
        private Link link;
        private MiniProgram miniprogram;
    }

    @Data
    public static class Image {
        private String mediaId;
    }

    @Data
    public static class Video {
        private String mediaId;
        private String thumbMediaId;
    }

    @Data
    public static class Link {
        private String title;
        private String url;
        private String picUrl;
        private String desc;
    }

    @Data
    public static class MiniProgram {
        private String title;
        private String picMediaId;
        private String appid;
        private String page;
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
