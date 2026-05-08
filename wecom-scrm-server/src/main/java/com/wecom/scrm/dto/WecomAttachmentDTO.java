package com.wecom.scrm.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class WecomAttachmentDTO {
    private String msgtype; // image, video, link, miniprogram
    private Image image;
    private Video video;
    private Link link;
    private MiniProgram miniprogram;

    @Data
    public static class Image {
        private String mediaId;
        private String picUrl;
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
        private String mediaId; // Used for Moments
        private String desc;
    }

    @Data
    public static class MiniProgram {
        private String title;
        private String picMediaId;
        private String appid;
        private String page;
    }
}
