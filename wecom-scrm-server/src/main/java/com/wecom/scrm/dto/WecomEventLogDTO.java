package com.wecom.scrm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WecomEventLogDTO {
    private Long id;
    private String corpId;
    private String msgType;
    private String event;
    private String changeType;
    private String externalUserid;
    private String userid;
    private String content;
    private Integer status;
    private Integer retryCount;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Extended fields
    private String userName;
    private String externalUserName;
    private String userAvatar;
    private String externalUserAvatar;

    // Group Chat fields
    private String chatId;
    private String groupName;
    private String updateDetail;
    private String memberId;
    private String memberName;
}
