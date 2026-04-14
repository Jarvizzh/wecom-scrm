package com.wecom.scrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_group_message")
@Data
@NoArgsConstructor
public class WecomGroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "send_type", columnDefinition = "TINYINT DEFAULT 0")
    private Integer sendType; // 0: Immediate, 1: Scheduled

    @Column(name = "send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    @Column(name = "target_type", columnDefinition = "TINYINT DEFAULT 0")
    private Integer targetType; // 0: All Groups, 1: Filtered Groups

    @Column(name = "target_condition", columnDefinition = "TEXT")
    private String targetCondition; // JSON: GroupMessageDTO.TargetCondition

    @Column(columnDefinition = "TEXT")
    private String content; // Text message content

    @Column(columnDefinition = "TEXT")
    private String attachments; // JSON: List of MomentDTO.Attachment

    @Column(columnDefinition = "TEXT")
    private String msgid; // WeCom returned msgid after sending

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0: Pending, 1: Sending, 2: Finished, 3: Failed

    @Column(name = "fail_msg", columnDefinition = "TEXT")
    private String failMsg;

    @Column(name = "creator_userid", length = 64)
    private String creatorUserid;

    @Column(name = "target_count")
    private Integer targetCount; // Number of matched group chats

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
