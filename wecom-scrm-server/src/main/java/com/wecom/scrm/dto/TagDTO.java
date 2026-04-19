package com.wecom.scrm.dto;

import lombok.Data;
import java.util.List;

public class TagDTO {

    @Data
    public static class BatchMarkTagsRequest {
        private String targetType; // "customer" or "yuewen"
        private List<TagTarget> targets;
        private List<String> addTagIds;
        private List<String> removeTagIds;
        
        // Select all support
        private boolean selectAll;
        private String customerName;
        private String unionid;
        private String employeeName;
        private String mpAppId;
        private List<String> tagIds;
        private Integer status;
        private boolean onlyDuplicates;

        // Yuewen filters
        private String appFlag;
        private String openid;
        private Long minAmount;
        private Long maxAmount;
    }

    @Data
    public static class TagTarget {
        private String userid;
        private String externalUserid;
    }
}
