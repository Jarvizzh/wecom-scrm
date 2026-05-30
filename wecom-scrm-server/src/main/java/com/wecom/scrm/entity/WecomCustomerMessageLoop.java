package com.wecom.scrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_customer_message_loop")
@Data
@NoArgsConstructor
public class WecomCustomerMessageLoop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "target_type", columnDefinition = "TINYINT DEFAULT 0")
    private Integer targetType; // 0: All, 1: Filtered

    @Column(name = "target_condition", columnDefinition = "TEXT")
    private String targetCondition;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "sender_list", columnDefinition = "TEXT")
    private String senderList;

    @Column(name = "creator_userid", length = 64)
    private String creatorUserid;

    @Column(name = "loop_type", nullable = false)
    private Integer loopType; // 1: Daily, 2: Weekly

    @Column(name = "loop_day_of_week", length = 64)
    private String loopDayOfWeek; // For weekly: comma-separated integers (e.g. "1,2,3")

    @Column(name = "send_time_of_day", nullable = false, length = 8)
    private String sendTimeOfDay; // HH:mm:ss (e.g. "10:00:00")

    @Column(name = "last_trigger_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTriggerTime;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 0: Disabled, 1: Enabled

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
