package com.wecom.scrm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for welcome message attachments stored in the database.
 */
@Data
public class WelcomeMessageAttachmentDTO {

    private String msgtype; // image, link, miniprogram
    
    private Image image;
    private Link link;
    private MiniProgram miniprogram;

    @Data
    public static class Image {
        @JsonProperty("media_id")
        private String mediaId;
        
        @JsonProperty("pic_url")
        private String picUrl;
    }

    @Data
    public static class Link {
        private String title;
        
        @JsonProperty("picurl")
        private String picUrl;
        
        private String desc;
        private String url;
    }

    @Data
    public static class MiniProgram {
        private String title;
        
        @JsonProperty("pic_media_id")
        private String picMediaId;
        
        private String appid;
        private String page;
    }
}
