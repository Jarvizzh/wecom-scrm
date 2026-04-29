package com.wecom.scrm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_event_log")
@Data
@NoArgsConstructor
public class WecomEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corp_id", nullable = false, length = 64)
    private String corpId;

    @Column(name = "msg_type", length = 32)
    private String msgType;

    @Column(length = 32)
    private String event;

    @Column(name = "change_type", length = 64)
    private String changeType;

    @Column(name = "external_userid", length = 64)
    private String externalUserid;

    @Column(length = 64)
    private String userid;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status = 0; // 0:Pending, 1:Success, 2:Failed, 3:Processing

    @Column(name = "retry_count", columnDefinition = "INT DEFAULT 0")
    private Integer retryCount = 0;

    @Lob
    @Column(name = "error_msg")
    private String errorMsg;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
