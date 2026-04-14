package com.wecom.scrm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMemberVO {
    private Long id;
    private String chatId;
    private String userid;
    private String memberName;
    private Integer type; // 1: Internal, 2: External
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;
    private Integer joinScene;
    private String invitor;
    private String avatar;
}
